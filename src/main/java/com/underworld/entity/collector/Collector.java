package com.underworld.entity.collector;

import com.underworld.registry.ModAttachments;
import com.underworld.registry.ModBlocks;
import com.underworld.registry.ModItems;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * THE COLLECTOR - hidden NPC who hoards forgotten things.
 *
 * <p>Not a merchant first: interacting starts a short suspicious intro; handing him an Ancient Artifact records it
 * (per player, see {@link ModAttachments#COLLECTOR_RECORD}) and gives a reward; otherwise he shows his small
 * stock of odd things. He cannot be killed - attacking him makes him vanish.
 */
public class Collector extends PathfinderMob implements Merchant {
	private static final EntityDataAccessor<Boolean> DATA_INSPECTING = SynchedEntityData.defineId(Collector.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_VANISHED = SynchedEntityData.defineId(Collector.class, EntityDataSerializers.BOOLEAN);
	private static final DustParticleOptions DUST = new DustParticleOptions(0x8B5CF6, 1.0F);
	private static final int HOME_RADIUS = 5;
	private static final int VANISH_TICKS = 160;

	private static final String MET = "met";
	private static final String SEEN_ARROW = "seen_hunter_arrow";
	private static final String SEEN_EYE = "seen_hunters_eye";
	private static final String RELIC_TOUCHED = "relic_touched";
	private static final String COMPLETED = "collection_complete";

	private static final String[] AMBIENT_LINES = {
		"I've been looking for that.",
		"Don't touch that.",
		"That belonged to someone.",
		"Everything down here has a story.",
		"Some things are better left buried.",
		"Interesting...",
	};

	private record Line(int at, UUID player, String text, @Nullable Runnable action) {
	}

	private final List<Line> queue = new ArrayList<>();
	private @Nullable Player tradingPlayer;
	private MerchantOffers offers = new MerchantOffers();
	private @Nullable BlockPos home;
	private int ambientCooldown = 600;
	private int vanishTicks;
	private int inspectTicks;

	public Collector(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 40.0)
			.add(Attributes.MOVEMENT_SPEED, 0.22)
			.add(Attributes.FOLLOW_RANGE, 16.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.9F));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_INSPECTING, false);
		builder.define(DATA_VANISHED, false);
	}

	public boolean isInspecting() {
		return this.entityData.get(DATA_INSPECTING);
	}

	public boolean isVanished() {
		return this.entityData.get(DATA_VANISHED);
	}

	public void setHome(BlockPos pos) {
		this.home = pos.immutable();
		this.setHomeTo(this.home, HOME_RADIUS);
	}

	// ================================================================= ticking
	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (this.home != null && !this.hasHome()) {
			this.setHomeTo(this.home, HOME_RADIUS);
		}

		if (this.vanishTicks > 0 && --this.vanishTicks == 0) {
			this.reappear(level);
		}

		if (this.inspectTicks > 0 && --this.inspectTicks == 0) {
			this.entityData.set(DATA_INSPECTING, false);
			this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
		if (this.isInspecting() || !this.queue.isEmpty() || this.tradingPlayer != null) {
			this.getNavigation().stop();
		}

		// collect due lines first: running an action may queue more lines
		List<Line> due = new ArrayList<>();
		for (Iterator<Line> it = this.queue.iterator(); it.hasNext(); ) {
			Line line = it.next();
			if (this.tickCount >= line.at()) {
				due.add(line);
				it.remove();
			}
		}
		for (Line line : due) {
			Player p = level.getPlayerByUUID(line.player());
			if (p == null) {
				continue;
			}
			if (line.text() != null && !line.text().isEmpty()) {
				this.say(p, line.text());
			}
			if (line.action() != null) {
				line.action().run();
			}
		}

		if (!this.isVanished() && this.queue.isEmpty() && --this.ambientCooldown <= 0) {
			this.ambientCooldown = 900 + this.random.nextInt(900);
			Player near = level.getNearestPlayer(this, 7.0);
			if (near != null && !near.isSpectator() && this.hasLineOfSight(near) && this.random.nextFloat() < 0.35F) {
				this.say(near, AMBIENT_LINES[this.random.nextInt(AMBIENT_LINES.length)]);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() && !this.isVanished() && this.random.nextInt(40) == 0) {
			// the satchel crystal leaks a little light now and then
			this.level().addParticle(DUST, this.getRandomX(0.4), this.getY() + 0.8, this.getRandomZ(0.4), 0, 0.01, 0);
		}
	}

	// ================================================================= dialogue
	private void say(Player player, String text) {
		player.sendSystemMessage(Component.empty()
			.append(Component.literal("The Collector").withStyle(ChatFormatting.DARK_PURPLE))
			.append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal(text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		this.playSound(SoundEvents.WANDERING_TRADER_AMBIENT, 0.7F, 0.55F + this.random.nextFloat() * 0.1F);
		this.getLookControl().setLookAt(player, 30.0F, 30.0F);
	}

	private void sayLater(Player player, int delay, String text) {
		this.queue.add(new Line(this.tickCount + delay, player.getUUID(), text, null));
	}

	private void doLater(Player player, int delay, Runnable action) {
		this.queue.add(new Line(this.tickCount + delay, player.getUUID(), "", action));
	}

	private boolean busyWith(Player player) {
		for (Line l : this.queue) {
			if (l.player().equals(player.getUUID())) {
				return true;
			}
		}
		return false;
	}

	// ================================================================= interaction
	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.isVanished() || !this.isAlive()) {
			return InteractionResult.PASS;
		}
		if (this.level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (this.busyWith(player) || this.isInspecting()) {
			return InteractionResult.SUCCESS;
		}

		ItemStack held = player.getItemInHand(hand);
		List<String> record = record(player);

		if (!record.contains(MET)) {
			addRecord(player, MET);
			this.say(player, "You weren't supposed to find this place.");
			this.sayLater(player, 50, "Interesting...");
			this.doLater(player, 80, () -> this.inspectEquipment(player));
			return InteractionResult.SUCCESS;
		}

		if (ModItems.ARTIFACTS.contains(held.getItem())) {
			this.receiveArtifact(player, held);
			return InteractionResult.SUCCESS;
		}
		if (held.is(ModItems.HOLLOW_ARROW) && !record.contains(SEEN_ARROW)) {
			addRecord(player, SEEN_ARROW);
			this.hold(held.copyWithCount(1), 45);
			this.say(player, "Ah...");
			this.sayLater(player, 40, "You found one of his arrows.");
			return InteractionResult.SUCCESS;
		}
		if (held.is(ModItems.HUNTERS_EYE) && !record.contains(SEEN_EYE)) {
			addRecord(player, SEEN_EYE);
			this.hold(held.copyWithCount(1), 90);
			this.say(player, "...");
			this.sayLater(player, 35, "You actually found him.");
			this.sayLater(player, 85, "I wondered if he was still hunting.");
			return InteractionResult.SUCCESS;
		}

		this.openTrades(player);
		return InteractionResult.SUCCESS;
	}

	private void inspectEquipment(Player player) {
		boolean hasGear = false;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.isArmor() && !player.getItemBySlot(slot).isEmpty()) {
				hasGear = true;
			}
		}
		if (!player.getMainHandItem().isEmpty()) {
			hasGear = true;
		}
		this.say(player, hasGear ? "You've been outside." : "You came down here with nothing?");
	}

	private void receiveArtifact(Player player, ItemStack held) {
		Item item = held.getItem();
		String id = idOf(item);
		if (record(player).contains(id)) {
			this.say(player, "I already have one of these.");
			this.sayLater(player, 40, "...Keep it. Someone else will want it.");
			return;
		}

		ItemStack shown = held.copyWithCount(1);
		if (!player.getAbilities().instabuild) {
			held.shrink(1);
		}
		addRecord(player, id);
		this.hold(shown, 110);
		this.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.7F);

		String[] lines = reactionTo(item);
		this.say(player, lines[0]);
		this.sayLater(player, 45, lines[1]);
		this.doLater(player, 95, () -> {
			for (ItemStack reward : rewardFor(item)) {
				giveOrDrop(player, reward);
			}
			this.playSound(SoundEvents.WANDERING_TRADER_YES, 0.8F, 0.6F);
			this.checkCompletion(player);
		});
	}

	private void checkCompletion(Player player) {
		List<String> record = record(player);
		if (record.contains(COMPLETED)) {
			return;
		}
		for (Item artifact : ModItems.ARTIFACTS) {
			if (!record.contains(idOf(artifact))) {
				return;
			}
		}
		addRecord(player, COMPLETED);
		this.sayLater(player, 40, "That's all of them. All five.");
		this.sayLater(player, 90, "Hm. You have an eye for this.");
		this.doLater(player, 110, () -> {
			giveOrDrop(player, enchantedBook(Enchantments.MENDING, 1));
			giveOrDrop(player, new ItemStack(ModItems.COLLECTOR_LEDGER));
		});
	}

	private static String[] reactionTo(Item item) {
		if (item == ModItems.BROKEN_SOUL_LANTERN) {
			return new String[]{"Where did you get this?", "...Never mind. The soul inside is still awake. I'll take it."};
		}
		if (item == ModItems.ANCIENT_DEATH_COIN) {
			return new String[]{"I've seen one of these before.", "This is older than the river."};
		}
		if (item == ModItems.WARDENS_SEAL) {
			return new String[]{"This is older than the Warden.", "...or it was made for him. Hard to say."};
		}
		if (item == ModItems.HOLLOW_CRYSTAL) {
			return new String[]{"Empty.", "Something drank it dry. This came from somewhere far below."};
		}
		return new String[]{"I've never seen one intact.", "Nobody remembers who wore it. Good."};
	}

	private List<ItemStack> rewardFor(Item item) {
		if (item == ModItems.BROKEN_SOUL_LANTERN) {
			return List.of(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.SOUL_LANTERN, 2));
		}
		if (item == ModItems.ANCIENT_DEATH_COIN) {
			return List.of(new ItemStack(Items.EMERALD, 12), new ItemStack(Items.GOLD_INGOT, 4));
		}
		if (item == ModItems.WARDENS_SEAL) {
			return List.of(new ItemStack(Items.ECHO_SHARD, 3), new ItemStack(Items.EMERALD, 10));
		}
		if (item == ModItems.HOLLOW_CRYSTAL) {
			return List.of(new ItemStack(Items.AMETHYST_SHARD, 12), new ItemStack(ModItems.HOLLOW_ARROW, 8));
		}
		return List.of(new ItemStack(Items.EMERALD, 16), this.enchantedBook(Enchantments.SOUL_SPEED, 3));
	}

	private ItemStack enchantedBook(net.minecraft.resources.ResourceKey<Enchantment> key, int lvl) {
		Optional<Holder.Reference<Enchantment>> holder = this.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
		return holder.map(h -> EnchantmentHelper.createBook(new EnchantmentInstance(h, lvl))).orElse(new ItemStack(Items.BOOK));
	}

	/** Called when a player pokes the sealed relic on his shelf. */
	public void onRelicTouched(Player player) {
		if (this.isVanished() || this.busyWith(player)) {
			return;
		}
		if (!record(player).contains(RELIC_TOUCHED)) {
			addRecord(player, RELIC_TOUCHED);
			this.say(player, "No.");
		} else {
			this.say(player, "That one isn't for sale.");
		}
		this.playSound(SoundEvents.WANDERING_TRADER_NO, 0.8F, 0.55F);
	}

	private void hold(ItemStack stack, int ticks) {
		this.setItemSlot(EquipmentSlot.MAINHAND, stack);
		this.entityData.set(DATA_INSPECTING, true);
		this.inspectTicks = ticks;
	}

	private static void giveOrDrop(Player player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.spawnAtLocation((ServerLevel) player.level(), stack);
		}
	}

	private static String idOf(Item item) {
		return item.builtInRegistryHolder().key().identifier().getPath();
	}

	public static List<String> record(Player player) {
		return player.getAttachedOrElse(ModAttachments.COLLECTOR_RECORD, List.of());
	}

	private static void addRecord(Player player, String entry) {
		List<String> next = new ArrayList<>(record(player));
		if (!next.contains(entry)) {
			next.add(entry);
		}
		player.setAttached(ModAttachments.COLLECTOR_RECORD, List.copyOf(next));
	}

	// ================================================================= trading
	private void openTrades(Player player) {
		if (this.tradingPlayer != null && this.tradingPlayer != player) {
			this.say(player, "Wait.");
			return;
		}
		this.offers = this.buildOffers(player);
		this.setTradingPlayer(player);
		this.openTradingScreen(player, Component.literal("The Collector"), 1);
	}

	private MerchantOffers buildOffers(Player player) {
		MerchantOffers list = new MerchantOffers();
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 10), new ItemStack(Items.ECHO_SHARD, 2), 3, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 14), Optional.of(new ItemCost(Items.ECHO_SHARD, 2)), new ItemStack(Items.RECOVERY_COMPASS), 1, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 22), Optional.of(new ItemCost(Items.BOOK)), this.enchantedBook(Enchantments.SWIFT_SNEAK, 2), 1, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 6), Optional.of(new ItemCost(Items.BONE, 4)), new ItemStack(Items.SKELETON_SKULL), 2, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 20), Optional.of(new ItemCost(Items.AMETHYST_SHARD, 4)), new ItemStack(ModItems.COLLECTOR_LEDGER), 1, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 8), new ItemStack(ModBlocks.SOUL_JAR), 2, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(ModBlocks.POTION_BOTTLES), 4, 0, 0.0F));
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(ModBlocks.SPECIMEN_JAR), 2, 0, 0.0F));
		if (record(player).contains(SEEN_EYE)) {
			list.add(new MerchantOffer(new ItemCost(Items.EMERALD, 12), new ItemStack(ModItems.HOLLOW_ARROW, 8), 4, 0, 0.0F));
		}
		return list;
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		this.tradingPlayer = player;
	}

	@Override
	public @Nullable Player getTradingPlayer() {
		return this.tradingPlayer;
	}

	@Override
	public MerchantOffers getOffers() {
		return this.offers;
	}

	@Override
	public void overrideOffers(MerchantOffers offers) {
		this.offers = offers;
	}

	@Override
	public void notifyTrade(MerchantOffer offer) {
		offer.increaseUses();
		this.ambientCooldown = Math.max(this.ambientCooldown, 200);
		if (this.tradingPlayer != null && this.random.nextFloat() < 0.4F) {
			this.say(this.tradingPlayer, this.random.nextBoolean() ? "Careful with that." : "It's yours now. Don't lose it.");
		}
	}

	@Override
	public void notifyTradeUpdated(ItemStack itemStack) {
	}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xp) {
	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.WANDERING_TRADER_YES;
	}

	@Override
	public boolean isClientSide() {
		return this.level().isClientSide();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tradingPlayer == player && this.isAlive() && !this.isVanished() && player.distanceToSqr(this) < 64.0;
	}

	// ================================================================= he is not a fighter
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.GENERIC_KILL)) {
			return super.hurtServer(level, source, damage);
		}
		if (!this.isVanished() && source.getEntity() instanceof Player player) {
			this.say(player, "You're making a mistake.");
			this.vanish(level);
		}
		return false;
	}

	private void vanish(ServerLevel level) {
		if (this.tradingPlayer instanceof ServerPlayer sp) {
			sp.closeContainer();
		}
		this.setTradingPlayer(null);
		this.queue.clear();
		Vec3 p = this.position();
		level.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 1, p.z, 20, 0.3, 0.6, 0.3, 0.02);
		level.sendParticles(DUST, p.x, p.y + 1, p.z, 25, 0.4, 0.7, 0.4, 0);
		level.playSound(null, p.x, p.y, p.z, SoundEvents.WANDERING_TRADER_DISAPPEARED, SoundSource.NEUTRAL, 1.0F, 0.6F);
		this.entityData.set(DATA_VANISHED, true);
		this.setInvisible(true);
		this.vanishTicks = VANISH_TICKS;
		if (this.home != null) {
			this.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
		}
	}

	private void reappear(ServerLevel level) {
		this.entityData.set(DATA_VANISHED, false);
		this.setInvisible(false);
		Vec3 p = this.position();
		level.sendParticles(DUST, p.x, p.y + 1, p.z, 20, 0.3, 0.6, 0.3, 0);
		level.playSound(null, p.x, p.y, p.z, SoundEvents.WANDERING_TRADER_REAPPEARED, SoundSource.NEUTRAL, 0.8F, 0.6F);
	}

	@Override
	public boolean isPickable() {
		return !this.isVanished() && super.isPickable();
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.WANDERING_TRADER_HURT;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.home != null) {
			output.putInt("HomeX", this.home.getX());
			output.putInt("HomeY", this.home.getY());
			output.putInt("HomeZ", this.home.getZ());
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		int y = input.getIntOr("HomeY", Integer.MIN_VALUE);
		if (y != Integer.MIN_VALUE) {
			this.setHome(new BlockPos(input.getIntOr("HomeX", 0), y, input.getIntOr("HomeZ", 0)));
		}
		this.entityData.set(DATA_VANISHED, false);
		this.setInvisible(false);
	}
}
