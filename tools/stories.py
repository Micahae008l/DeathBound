"""The DeathBound stories: chest books (each handed out once per world) and notes placed in the world.
Writes their text into en_us.json as story.deathbound.<id>.title / .author / .p1..pN.
Keep the ids in step with story/Stories.java (BOOKS and NOTES: a note's index is its blockstate "story" value)."""
import json

LANG = "src/main/resources/assets/deathbound/lang/en_us.json"

# Style: a real person telling what happened, in their own voice. Plain and concrete, no poetic lines.
# Every book is a different death, every detail true to the game, and most end with a lesson or a punchline.
# No em dashes.
BOOKS = [
    ("sand_keeps_time", "Day Six", "Talia, treasure hunter", [
        "Day 1. Found a desert temple and dug down to the treasure room. My pickaxe broke on the last block.",
        "Four chests of gold, diamonds and rotten flesh. No pickaxe, nothing to stand on, and the hole is too high to jump.",
        "Day 4. Ate the rotten flesh. Do not eat the rotten flesh.",
        "Day 6. I have eleven diamonds and no wood to make a pickaxe. If you go into a temple, bring a spare pickaxe.",
    ]),
    ("three_skulls", "Three Skulls", "Corin", [
        "To my brother Aldric. You said I'd never build the Wither. Well, I got the third skull today. Forty trips to the Nether fortress.",
        "I built it in the middle of the village so everyone could watch me beat it.",
        "Quick update. It's not going well. It blew up the church, the blacksmith and most of the villagers. Now it's flying toward your house.",
        "Sorry about your house. You were right.",
    ]),
    ("bait", "Nine Hours", "Edda, miner", [
        "I'd been mining for nine hours and found nothing. Then I saw four diamond ores in the wall of a ravine.",
        "I mined the first one, and lava poured out of the hole behind it. I stepped back, right into more lava.",
        "Everything burned, the diamonds too. Always carry a water bucket. Always.",
    ]),
    ("a_soft_hiss", "Two Years of Work", "Bram, farmer", [
        "I spent two years on my farm. Wheat, carrots, a pumpkin patch, forty cows and a house with a real glass window.",
        "I never put torches around it. I thought they looked ugly next to the crops.",
        "Last night I was harvesting the wheat and heard a hiss right behind me. I didn't even have time to turn around.",
        "Put up the torches. They look fine. A crater looks worse.",
    ]),
    ("go_to_bed", "Go To Bed", "Lio", [
        "I hadn't slept in four days. I was building a castle and didn't want to stop. Beds are for people with no plans.",
        "On the fifth night, big blue flying things started diving at me out of the sky. Three of them. Then five.",
        "I ran for my bed. It was on the other side of the castle. I had built a really big castle.",
    ]),
    ("under_me_stars", "The Dragon Egg", "Saro", [
        "I killed the Ender Dragon. Alone! I ran around the island screaming.",
        "Then I went for the egg. When you hit it, it teleports, so I chased it all the way to the edge of the island.",
        "An enderman was standing there. I looked at it by accident. It screamed, and I took one step back.",
        "There was nothing behind me. I never got the egg.",
    ]),
    ("wear_the_gold", "Wear The Gold", "Fenna", [
        "Everyone says piglins leave you alone if you wear one piece of gold. I had my gold boots on.",
        "Then I opened a chest in their bastion. They don't like that, gold or no gold. Nobody tells you that part.",
        "Twenty piglins and one very angry brute. My boots are still there if you want them.",
    ]),
    ("a_bed_in_the_nether", "Good Night", "Ivo", [
        "My first trip to the Nether. It was getting late and I was tired, so I put down my bed to skip the night.",
        "There is no night in the Nether. There is no day either.",
        "The bed exploded and took me and half the fortress with it. Do not sleep in the Nether.",
    ]),
    ("the_cat_is_patient", "The Cat Is Patient", "Mabel", [
        "I didn't die fighting anything. I got old in my house by the river, with my cat Biscuit.",
        "One night I went to bed and woke up here, standing in a line. Biscuit was in my arms. I don't know how he got here.",
        "The line hasn't moved in a long time. Biscuit doesn't mind, he sleeps a lot. If you ever reach the front, tell them we're still waiting.",
    ]),
    ("last_torch", "The Last Torch", "Wren, explorer", [
        "I went into the cave with sixty-four torches and put one down every ten blocks.",
        "Four hours later I found a wall full of iron. I put down my last torch and started mining.",
        "I got so into it that I wandered out of the light. It's very dark out there.",
        "Skeletons. A lot of them. I couldn't see where the arrows came from. Bring two stacks of torches, not one.",
    ]),
    ("light_as_snow", "Powder Snow", "Oskar", [
        "I was climbing a mountain for the view. There was a patch of snow on the path that looked exactly like all the other snow.",
        "It wasn't. It was powder snow. I fell straight through it and couldn't climb back out.",
        "I wasn't wearing leather boots. That's the whole story. Wear leather boots on mountains.",
    ]),
    ("wings", "Wings", "Juno", [
        "I found elytra on a ship in the End. I put them on and jumped off the ship screaming like a kid.",
        "I flew home with my rockets. I didn't count them. I should have counted them.",
        "I ran out above the mountains, way up high. I tried to glide down slowly. I didn't glide slowly enough.",
    ]),
    ("shh", "Shh", "Ada, explorer", [
        "Deep under the caves I found an ancient city. Black sculk everywhere, and chests full of good loot.",
        "I sneaked the whole way. I was so careful. Then I stepped on a shrieker. It screamed. I stepped on another one.",
        "The ground shook, and the Warden dug its way up out of the floor. It can't see, but it can smell you. It took two hits.",
    ]),
    ("bad_omen", "Bad Omen", "Captain Hode", [
        "I killed a pillager captain and took his banner home. After that I felt strange, like something was following me.",
        "The next day I walked into a village to trade. The bell started ringing and every villager ran inside.",
        "Pillagers, axe men, witches, and a ravager that walked straight through a house. I fought four waves. The fifth got me.",
    ]),
    ("under_the_sea", "Under The Sea", "Hob, diver", [
        "I found an ocean monument, a big green building on the sea floor. There's gold inside, they say.",
        "I swam in with three water breathing potions. A huge grey fish looked at me, and suddenly I could barely break a block.",
        "Then the small fish started shooting lasers. My last potion ran out halfway back to the surface.",
    ]),
    ("kid", "The Goat", "Hal, shepherd", [
        "I climbed a mountain for a goat horn. When a goat rams into a rock, its horn falls off.",
        "So I stood in front of a rock and waited for a goat to charge at me.",
        "It charged. I jumped out of the way too late, and it knocked me off the mountain. The rock was fine.",
    ]),
    ("channeling", "Channeling", "Ines, smith", [
        "I got a trident with Channeling. Throw it at something during a thunderstorm, and lightning strikes what you hit.",
        "A zombie came at me in the rain. It was right in front of me. I threw the trident anyway.",
        "Lightning doesn't only hit the target. It hits everything next to it. I was next to it.",
    ]),
    ("small_blue_spiders", "Small Blue Spiders", "Tam", [
        "I found an old mineshaft and followed the rails down. Cobwebs everywhere.",
        "Then I found the spawner. Cave spiders, small and blue and fast, coming out of the cage one after another.",
        "Their bites poison you. I got out with half a heart left and no food. Then a zombie came around the corner.",
    ]),
    ("straight_down", "Straight Down", "Nils, miner", [
        "Rule one of mining: never dig straight down. I knew the rule. I did it anyway, because it was faster.",
        "I dug forty blocks down with no problems. I thought the rule was dumb.",
        "On block forty-one the floor opened into a huge cave, and I fell all the way to the bottom. The rule is not dumb.",
    ]),
    ("the_nice_lady", "The Witch", "Pim", [
        "I found a little hut on stilts in the swamp. Inside was an old lady in a purple robe, with a black cat.",
        "I thought she was a villager and walked up to trade. She threw a potion at me. Slowness. Then poison.",
        "Every time I hit her, she drank something and healed. Witches aren't nice. The cat was fine, though.",
    ]),
    ("return_to_sender", "Return To Sender", "Dov", [
        "I built a bridge across a lava lake in the Nether. Cobblestone, one block wide, nice and straight.",
        "A ghast floated out of the smoke and started shooting fireballs at me. I ran for the other side.",
        "A fireball hit the bridge right in front of me. You can hit fireballs back at a ghast with your sword. I know that now.",
    ]),
    ("the_stone_moved", "Silverfish", "Uma", [
        "I was looking for the End portal in a stronghold. Stone brick halls and iron doors, all the way down.",
        "I broke a stone brick in the wall, and a silverfish crawled out. I hit it.",
        "When you hit a silverfish, it calls the others out of the walls. The whole hallway came alive. Don't hit the first one. Just walk away.",
    ]),
    ("bad_trade", "Bad Trade", "Rook", [
        "A librarian wanted twenty-four emeralds for a Mending book. I had twenty.",
        "I got mad and punched him.",
        "The village iron golem saw. It threw me so high I could see three biomes. Then I came back down.",
    ]),
    ("cactus_fence", "Cactus Fence", "Della", [
        "Zombies kept walking into my garden at night, so I planted a cactus wall all the way around it. It took a week.",
        "It worked. No more zombies. I was really proud of it.",
        "Then a skeleton started shooting at me from outside. I backed away from the arrows, right into my own cactus wall. Twice.",
    ]),
]

# notes: index = blockstate "story" value; title says where it is, author is the signature ("" for none)
# a paragraph starting with "~" was added by someone else: the reading screen shows it in another hand
NOTES = [
    ("note_ferry", "Nailed to the post", "The Ferryman", [
        "NO COIN, NO CROSSING.",
        "~No coin? He sells charms. Each one carries you across once. Don't lose yours.",
    ]),
    ("note_lights", "Pinned to a tree", "", [
        "Don't follow the lights between the trees. They aren't lanterns.",
        "They aren't anything.",
    ]),
    ("note_marks", "Pinned to a tree", "R.", [
        "Day three in the woods. I mark every tree I pass with an X, so I don't walk in circles.",
        "This morning every tree has an X. I didn't make most of them.",
    ]),
    ("note_shed", "Tacked to the bark", "a friend", [
        "If you came for the old boat shed, take what you need and run.",
        "The roots are slow. The wood is not.",
    ]),
    ("note_list", "A list on the table", "Bea", [
        "Bread. Candles. Thread for Mira's ribbon.",
        "Ask Aldous about the Door.",
        "Feed the cat.",
        "~(There is no cat.)",
    ]),
    ("note_yours", "Left on the table", "Odo", [
        "To whoever finds this house: it's yours. The kettle sticks and the roof leaks on the left.",
        "I've gone to stand in the line. Don't wait up.",
    ]),
    ("note_tib", "Folded very small", "Tib", [
        "Pip says I kicked his ball off the bridge on purpose.",
        "I DIDN'T. Mostly.",
    ]),
    ("note_lamps", "The Lamplighter's rounds", "L.", [
        "The well. The bell tower. The bridge. The forge. The gate.",
        "Fewer every night. Light the ones that are left. Don't count the rest.",
    ]),
]

# shared lines the reading screen needs (these keys are wiped and rewritten with the stories)
SHARED = {
    "story.deathbound.signed": "from %s",
}

if __name__ == "__main__":
    lang = json.load(open(LANG))
    for k in [k for k in lang if k.startswith("story.deathbound.")]:
        del lang[k]
    lang.update(SHARED)
    for sid, title, author, paras in BOOKS + NOTES:
        key = f"story.deathbound.{sid}"
        lang[key + ".title"] = title
        if author:
            lang[key + ".author"] = author
        for i, p in enumerate(paras, 1):
            if p.startswith("~"):
                lang[f"{key}.p{i}.hand"] = "other"
            lang[f"{key}.p{i}"] = p.lstrip("~")
    with open(LANG, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(len(BOOKS), "books,", len(NOTES), "notes")
    print("BOOKS = " + ", ".join('"%s"' % b[0] for b in BOOKS))
    print("NOTES = " + ", ".join('"%s"' % n[0] for n in NOTES))
