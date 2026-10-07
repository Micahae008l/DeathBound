"""The DeathBound stories: chest books (each handed out once per world) and notes placed in the world.
Writes their text into en_us.json as story.deathbound.<id>.title / .author / .p1..pN.
Keep the ids in step with lore/Stories.java (BOOKS and NOTES: a note's index is its blockstate "story" value)."""
import json

LANG = "src/main/resources/assets/deathbound/lang/en_us.json"

BOOKS = [
    ("sand_keeps_time", "Sand Keeps Time", "Talia, treasure hunter", [
        "Day 1. A temple in the desert. Blue clay in the floor, shaped like an eye. I knew the treasure was under it.",
        "I broke the eye and fell. The treasure was there. So was the dark. The walls were too smooth to climb.",
        "Day 6. Gold and diamonds, and no bread. I'd trade every gem for one loaf.",
        "Sand trickles from the cracks, slow as a clock. I think it's counting for me.",
    ]),
    ("three_skulls", "Three Skulls", "Corin", [
        "Three skulls. Four blocks of soul sand. I set the last skull, and the sky went black.",
        "It had three heads, and all of them hated me. My sword was a twig. My armor was paper.",
        "I thought I was a hero. I was only the one who woke it. I'm sorry, everyone who lived nearby.",
    ]),
    ("bait", "Bait", "Edda, miner", [
        "Diamonds! Four of them, shining at the bottom of the cave. I swung my pick, and the floor gave way.",
        "It wasn't water underneath. It was orange, and loud, and very warm.",
        "If you see diamonds next to lava, leave them. They're bait. They were always bait.",
    ]),
    ("something_listening", "Something Listening", "Pell", [
        "Deep down, the stone turns blue and breathes. Something under it was listening.",
        "I crept. I crouched. I held my breath. Then a little shrieker screamed, and the ground began to shake.",
        "It had no eyes. It didn't need them. It heard my heart, and followed the sound.",
    ]),
    ("a_soft_hiss", "A Soft Hiss", "Bram, farmer", [
        "I built my house with my own hands. Oak walls, a glass window, a field of wheat.",
        "I was looking at my wheat when I heard it. A soft little hiss, right behind me.",
        "I never turned around. I'm glad. I don't think I wanted to see it smile.",
    ]),
    ("go_to_bed", "Go To Bed", "Lio", [
        "Three nights without sleep. There was always one more tunnel, one more ore.",
        "On the fourth night the sky grew wings. They screamed as they dove, blue and torn and endless.",
        "If you're reading this, go to bed. Please. Just go to bed.",
    ]),
    ("under_me_stars", "Under Me, Stars", "Saro", [
        "I beat the dragon. I stood at the edge of the world and laughed.",
        "An enderman stared at me, and I stepped back to get a better look.",
        "There was no ground. Only stars, above me and below me, and a long, quiet fall. It was almost pretty.",
    ]),
    ("wear_the_gold", "Wear The Gold", "Fenna", [
        "Note to self: the pig folk like gold. I left my gold boots by the portal.",
        "They did not like that. There were very many of them, and they were very fast.",
        "Wear the gold. Even if it looks silly. Especially then.",
    ]),
    ("a_bed_in_the_nether", "A Bed In The Nether", "Ivo", [
        "It was late, and home was far. I thought: I'll just sleep here, in my red bed.",
        "The bed did not agree.",
        "My last thought was how pretty the blast looked against the netherrack. Silly, what you think about.",
    ]),
    ("green_light", "Green Light", "Marin, diver", [
        "The sea temple glowed green under the waves. I took a deep breath and swam down.",
        "A great fish watched me with its one eye. My arms grew heavy. My breath ran out.",
        "The light from the surface looked so close. It wasn't.",
    ]),
    ("the_gate", "The Gate", "Hollis", [
        "I drank the dark bottle the captain dropped. I thought it was a prize.",
        "That night the bells rang and the villagers ran. The raiders came with axes and banners.",
        "I held the gate as long as I could. The villagers lived. I didn't. That seems fair.",
    ]),
    ("so_soft", "So Soft", "Nel", [
        "Fresh snow on the mountain. So soft. So white.",
        "I stepped in and kept sinking. It wasn't snow. It was powder, and it was very cold.",
        "I could see the sun the whole time. It just wasn't warm enough.",
    ]),
    ("for_a_second", "For A Second", "Aya", [
        "Wings! I found wings on a ship at the end of the world. I flew over oceans and mountains.",
        "I went faster, and faster. The cliff came up faster than that.",
        "For a second, I really was flying.",
    ]),
    ("small_blue_spiders", "Small Blue Spiders", "Tam", [
        "An old mineshaft, full of rails and cobwebs. I followed the rails down.",
        "The webs got thicker. Small blue spiders poured out of a cage. Then more. Then more.",
        "I feel sick and everything looks green. There's a minecart here. I think I'll sit in it a while.",
    ]),
    ("the_cat_is_patient", "The Cat Is Patient", "Mabel", [
        "I lived a long time in my little house by the river. A cat, a garden, a good view.",
        "One night I lay down and didn't get up. I wasn't scared. I thought I'd see the river again.",
        "But the river is dry and the line is long. I'm still waiting, my cat in my arms. He's patient. So am I.",
    ]),
    ("last_torch", "The Last Torch", "Wren, spelunker", [
        "I always bring a stack of torches. Sixty-four. I placed one every ten steps going down.",
        "At the bottom of the ravine I found iron, gold, a whole wall of coal. I placed my last torch and kept digging.",
        "When I turned around, the way back was black. Somewhere in the black, something hissed.",
        "Bring more torches than you think you need. Then bring more than that.",
    ]),
    ("light_as_snow", "Light As Snow", "Oskar", [
        "The mountain was beautiful. White on white, and the snow looked soft enough to sleep in.",
        "I stepped off the path for a better view. The ground wasn't ground. I sank to my neck, and kept sinking.",
        "It's very quiet under the snow. My hands stopped hurting after a while. That's how I knew.",
    ]),
    ("wings", "Wings", "Juno", [
        "I found wings in a city at the end of the world. Purple towers, a ship in the sky, and a pair of wings.",
        "I flew home over the ocean, laughing, rockets in my hand. Seventeen rockets. Then sixteen. Then none.",
        "The ocean was a long way down. I had plenty of time to count how many rockets I should have brought.",
    ]),
    ("shh", "Shh", "Ada, explorer", [
        "Deep under the caves there's a city. Grey stone, blue fire, and black moss that whispers when you step on it.",
        "I crept. I crouched. I held my breath. Then I opened a chest, and the moss screamed.",
        "It can't see. It doesn't need to. It hears your heart. Mine was very loud.",
    ]),
    ("bad_omen", "Bad Omen", "Captain Hode", [
        "I killed their captain and took his banner. It looked good on my wall.",
        "I felt strange walking into the village. Then the bell rang. Then the horns.",
        "They came in waves, with axes and spells and a beast that walked straight through my door. The villagers hid. I didn't have time to.",
    ]),
    ("under_the_sea", "Under the Sea", "Hob, diver", [
        "A temple under the sea, green and lit from inside. I swam down to look.",
        "A great eye looked back at me, and my arms went weak. I couldn't swing. I could barely swim.",
        "Lasers. I didn't know fish had lasers. I know now.",
    ]),
    ("one_good_throw", "One Good Throw", "Kit", [
        "In the End, the islands float on nothing. I wanted the next island, and I had an ender pearl.",
        "I threw it as hard as I could. It sailed and sailed, and fell short.",
        "I followed it down. I think the pearl and I are still falling. It's very dark. It's very far.",
    ]),
    ("kid", "Kid", "Hal, shepherd", [
        "I climbed the mountain for a goat horn. I'd heard they make a lovely sound.",
        "A goat lowered its head and looked at me. I thought it was being friendly.",
        "I didn't get a goat horn. I got a very good view of the valley, very quickly.",
    ]),
    ("channeling", "Channeling", "Ines, smith", [
        "Storms never scared me. I liked to stand in the rain with my new trident and watch the sky.",
        "The trident was enchanted. Channeling, the book said. I never asked what it channeled.",
        "Now I know. The sky has a voice, and it is very, very loud.",
    ]),
]

# notes: index = blockstate "story" value; title says where it is, author is the signature ("" for none)
NOTES = [
    ("note_ferry", "Nailed to the post", "The Ferryman", [
        "NO COIN, NO CROSSING.",
        "Scratched underneath, in another hand: he'll take a charm instead. Ask nicely.",
    ]),
    ("note_lights", "Pinned to a tree", "", [
        "Don't follow the lights between the trees. They aren't lanterns.",
        "They aren't anything.",
    ]),
    ("note_marks", "Pinned to a tree", "R.", [
        "Day three in the wood. I mark every tree I pass with an X so I don't walk in circles.",
        "This morning every tree has an X. I didn't make most of them.",
    ]),
    ("note_shed", "Tacked to the bark", "a friend", [
        "If you came for the old boat-shed, take what you need and run.",
        "The roots don't like thieves. They're slow. The wood isn't.",
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
        "Pip says I kicked the ball off the bridge on purpose.",
        "I DIDN'T. Mostly.",
    ]),
    ("note_lamps", "The Lamplighter's rounds", "L.", [
        "The well. The bell tower. The bridge. The forge. The gate.",
        "Fewer every night. Light the ones that are left, and don't count the rest.",
    ]),
]


if __name__ == "__main__":
    lang = json.load(open(LANG))
    for k in [k for k in lang if k.startswith("story.deathbound.")]:
        del lang[k]
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
