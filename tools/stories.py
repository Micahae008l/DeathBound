"""The DeathBound stories: chest books (each handed out once per world) and notes placed in the world.
Writes their text into en_us.json as story.deathbound.<id>.title / .author / .p1..pN.
Keep the ids in step with story/Stories.java (BOOKS and NOTES: a note's index is its blockstate "story" value)."""
import json

LANG = "src/main/resources/assets/deathbound/lang/en_us.json"

# Style: plain words and short sentences, one strange image each, and an ending that leaves something unanswered.
# No em dashes. Every book is a different way to die.
BOOKS = [
    ("sand_keeps_time", "Sand Keeps Time", "Talia, treasure hunter", [
        "Day one. A temple in the desert, and a blue stone in the floor shaped like an eye. I knew the gold was under it.",
        "I dug, and the floor fell away. Gold, diamonds, and walls too high to climb.",
        "Day six. I would trade every gem in here for one loaf of bread.",
        "Sand falls from the ceiling, grain by grain. I think it's counting for me.",
    ]),
    ("three_skulls", "Three Skulls", "Corin", [
        "Soul sand in the shape of a T. Three black skulls on top. I placed the last one, and the sky went dark.",
        "It had three heads, and all three knew my name. My sword was a twig. My armor was paper.",
        "I wanted to be a hero. I was only the one who woke it. Sorry, everyone who lived nearby.",
    ]),
    ("bait", "Bait", "Edda, miner", [
        "Four diamonds, shining at the bottom of a cave. I ran to them.",
        "The stone under my feet broke. Under it was something orange, and loud, and very warm.",
        "If you see diamonds next to lava, walk away. Some things shine only to bring you closer.",
    ]),
    ("a_soft_hiss", "A Soft Hiss", "Bram, farmer", [
        "I built my house with my own hands. Oak walls, a glass window, a field of wheat.",
        "One evening I stood in the field, watching the wheat move in the wind. Then I heard a soft hiss behind me.",
        "I never turned around. I'm glad. Some faces you don't need to see.",
    ]),
    ("go_to_bed", "Go To Bed", "Lio", [
        "Three nights without sleep. There was always one more tunnel, one more ore.",
        "On the fourth night the sky grew wings. They were blue and torn, and they screamed as they fell on me.",
        "If you are reading this, go to bed. The night remembers who stays awake.",
    ]),
    ("under_me_stars", "Under Me, Stars", "Saro", [
        "I beat the dragon. I stood at the edge of the world and laughed.",
        "A tall dark thing was staring at me. I took one step back.",
        "There was no ground. Only stars above me and stars below me, and a long, quiet fall. It was almost pretty.",
    ]),
    ("wear_the_gold", "Wear The Gold", "Fenna", [
        "Everyone said the pig folk love gold. I took my gold boots off. They looked silly.",
        "The pig folk saw my bare feet. Then there were a lot of them, and they were very fast.",
        "Wear the gold. Even if it looks silly. Especially then.",
    ]),
    ("a_bed_in_the_nether", "A Bed In The Nether", "Ivo", [
        "It was late, and home was far. I thought: I'll just sleep here, in my red bed.",
        "The bed had other ideas.",
        "My last thought was how pretty the fire looked on the red stone. Strange, what you think about at the end.",
    ]),
    ("the_cat_is_patient", "The Cat Is Patient", "Mabel", [
        "I had a long life in a little house by the river. A cat, a garden, a good view.",
        "One night I lay down and didn't get up. I wasn't scared. I thought I would see the river again.",
        "But this river is dry, and the line is long. I'm still waiting, my cat in my arms. He is patient. So am I.",
    ]),
    ("last_torch", "The Last Torch", "Wren, explorer", [
        "I took sixty-four torches into the cave and placed one every ten steps.",
        "At the bottom I found iron, gold, a whole wall of coal. I placed my last torch and kept digging.",
        "When I turned around, the way back was dark. Something in the dark was putting my torches out, one by one.",
        "It never came closer. It didn't need to. It only had to wait.",
    ]),
    ("light_as_snow", "Light As Snow", "Oskar", [
        "The snow on the mountain looked soft enough to sleep in.",
        "I stepped off the path for a better view. The snow opened like a mouth, and I sank.",
        "It's very quiet under the snow. After a while my hands stopped hurting. That's how I knew.",
    ]),
    ("wings", "Wings", "Juno", [
        "I found wings on a ship at the end of the world, floating over nothing.",
        "I flew home over the sea, laughing. Seventeen rockets. Then sixteen. Then none.",
        "The sea was a long way down. I had plenty of time to count the rockets I should have brought.",
    ]),
    ("shh", "Shh", "Ada, explorer", [
        "Deep under the caves there is a city. Grey stone, blue fire, and black moss that whispers when you step on it.",
        "I walked softly. I held my breath. Then I opened a chest, and the moss screamed.",
        "The thing that came has no eyes. It doesn't need them. It heard my heart, and my heart was very loud.",
    ]),
    ("bad_omen", "Bad Omen", "Captain Hode", [
        "I killed a raider captain and took his banner. It looked good on my wall.",
        "When I walked into the village, the air felt wrong. Then the bell rang. Then the horns.",
        "They came in waves, with axes and spells and a beast that walked through my door. The villagers hid. I didn't have time to.",
    ]),
    ("under_the_sea", "Under The Sea", "Hob, diver", [
        "A temple under the sea, glowing green from inside. I swam down to look.",
        "A great eye opened in the water and looked back at me. My arms went weak.",
        "Then the eye began to glow. I didn't know fish could do that. I know now.",
    ]),
    ("kid", "Kid", "Hal, shepherd", [
        "I climbed the mountain for a goat horn. They say it sounds like the wind calling you home.",
        "A goat lowered its head and looked at me. I thought it was being friendly.",
        "I never got the horn. I got a very good view of the valley, very quickly.",
    ]),
    ("channeling", "Channeling", "Ines, smith", [
        "Storms never scared me. I had a new trident, and the book said it could call the lightning.",
        "A zombie came at me through the rain. I threw the trident. It was standing very close.",
        "The sky answered. It doesn't care who it hits. Remember that, when you call it.",
    ]),
    ("small_blue_spiders", "Small Blue Spiders", "Tam", [
        "An old mine full of rails and cobwebs. I followed the rails down into the dark.",
        "Small blue spiders came out of a cage. Then more. Then more.",
        "Every bite made me sicker. I found a minecart and sat down in it to rest. I'm still resting.",
    ]),
    ("straight_down", "Straight Down", "Nils, miner", [
        "Everyone says never dig straight down. I thought it was just something people say.",
        "I dug down and down. It was quick, and the stone was quiet.",
        "Then the floor broke. Under it was a cave so big it had its own wind. I fell for a long time.",
    ]),
    ("the_nice_lady", "The Nice Lady", "Pim", [
        "A little hut in the swamp, on thin wooden legs. A lady in a purple robe lived there, with a cat.",
        "I waved. She smiled, and threw a bottle at me. Then another.",
        "I grew slow, then weak, then sick. She laughed the whole time. The cat watched. It never blinked.",
    ]),
    ("return_to_sender", "Return To Sender", "Dov", [
        "I built a bridge across the lava lake, one block wide.",
        "Something white floated out of the smoke. It cried like a baby, and spat fire at me.",
        "You can hit the fire back. I found that out on the way down.",
    ]),
    ("the_stone_moved", "The Stone Moved", "Uma", [
        "Deep underground there's an old stone fort, all halls and iron doors. I went looking for the way to the End.",
        "I broke one stone in the wall, and a little grey bug crawled out. I hit it.",
        "Then the walls began to move. Every stone was full of them. If you meet the first one, don't hit it. Just run.",
    ]),
    ("bad_trade", "Bad Trade", "Rook", [
        "The villager wanted twenty emeralds for one old book. I got angry, and I hit him.",
        "Behind me, something very big and made of iron turned around. It was holding a red flower.",
        "It threw me into the sky. For a moment I could see the whole village, small and safe. Then I came down.",
    ]),
    ("cactus_fence", "Cactus Fence", "Della", [
        "The zombies kept walking into my garden. So I grew a cactus fence all the way around it.",
        "It worked. Nothing got in. Then one night a skeleton shot at me from the dark, and I stepped back.",
        "I was very proud of that fence. It was a good fence. It still is.",
    ]),
]

# notes: index = blockstate "story" value; title says where it is, author is the signature ("" for none)
NOTES = [
    ("note_ferry", "Nailed to the post", "The Ferryman", [
        "NO COIN, NO CROSSING.",
        "Scratched under it, in another hand: he takes charms too. Ask nicely.",
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
        "Feed the cat. (There's no cat. There used to be a cat.)",
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
            lang[f"{key}.p{i}"] = p
    with open(LANG, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(len(BOOKS), "books,", len(NOTES), "notes")
    print("BOOKS = " + ", ".join('"%s"' % b[0] for b in BOOKS))
    print("NOTES = " + ", ".join('"%s"' % n[0] for n in NOTES))
