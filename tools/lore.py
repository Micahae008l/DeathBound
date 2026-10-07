"""The Underworld's lore: NPC dialogue, lore books, the dead's last words, area titles, boss fight lines, sounds.

The story, in plain words (most of it is hidden; the player pieces it together):
  * The dead cannot rule the dead. The Underworld always has a living King, someone who came down alive wearing a
    chain, beat the old King and took the throne. The Warden serves the throne, whoever sits on it. The King keeps
    the order: the Ferryman rows souls down the River of the Dead, the Warden judges them, the good pass the Great
    Door into the Lake of Souls and rest.
  * Kings before: Halden, Ysolde, then the King. The King grew old on the throne and afraid of being replaced, so he
    hid the chains.
  * Silas, a living player, found a chain anyway, came down to "beat the Underworld", killed the King and took the
    throne. The Warden knelt. Then the King (alive, because the dead cannot die) told him the rest of the rule: one
    day another chain comes down and takes it from you.
  * So Silas made himself impossible to replace. He shut the Door, ate souls, drank the Lake of Souls dry, melted
    every chain he could find. He calls himself the Death King. One chain got away: the player's.
  * The Prophet in the crypt is the old King, blinded and chained by Silas. He sent the player's chain up, and the
    one Aldous wears too, his spare. He wants his throne back.
  * The Ferryman rowed every King down. The Hollow Hunter was the first living soul ever to come down; he refused
    the throne and hunts instead. The Collector keeps what the Kings left behind.
  * When the Death King falls, the player brings his Heart to the empty throne and chooses: take it (the cycle goes
    on), free the old King (order, and the chains hidden again), or break it (no more kings; everyone moves on).
"""
import os

NS = 'deathbound'

# ---------------------------------------------------------------------------------------------------- dialogue
VOICE = {'ferryman': '#cdc4ae', 'gravedigger': '#dcc9a8', 'prophet': '#c7b2ff', 'king': '#e2d3ff', 'throne': '#d9c7f2',
         'collector': '#c9d6b8'}
NAME = {'ferryman': ('The Ferryman', '#9d8f6e'), 'gravedigger': ('Aldous, the Traveler', '#b08d5c'),
        'prophet': ('The Chained Prophet', '#9b7be0'), 'king': ('The Old King', '#c9a8ff'),
        'throne': ('The Empty Throne', '#b48cff'), 'collector': ('The Collector', '#8fa877'),
        'bonesmith': ('Clatter, the Bonesmith', '#a3adbd'), 'mira': ('Mira', '#a9bfe0'), 'lamplighter': ('The Lamplighter', '#b9b2c9'),
        'sentry': ('The Sentry', '#8fa4c2'), 'kid': ('Pip', '#c7bda8')}
VOICE.update({'bonesmith': '#cdd3dd', 'mira': '#d6e2f5', 'lamplighter': '#d8d2e2', 'sentry': '#c2d0e2', 'kid': '#e6dccb'})


def _page(npc, paragraphs):
    return [{'type': 'minecraft:plain_message', 'width': 310,
             'contents': {'text': p.strip('*'), 'color': VOICE[npc], **({'italic': True} if p.startswith('*') else {})}}
            for p in paragraphs]


def _dialog(npc, paragraphs, buttons, leave='Leave'):
    """buttons: (label, target[, tooltip]); a target 'custom:id' or 'custom:id/payload' calls back to the server."""
    title, color = NAME[npc]
    actions = []
    for label, target, *tip in buttons:
        if target.startswith('custom:'):
            cid, _, payload = target[7:].partition('/')
            action = {'type': 'minecraft:custom', 'id': f'{NS}:{cid}', **({'payload': payload} if payload else {})}
        else:
            action = {'type': 'minecraft:show_dialog', 'dialog': f'{NS}:{target}'}
        button = {'label': {'text': label}, 'width': 250, 'action': action}
        if tip:
            button['tooltip'] = {'text': tip[0]}
        actions.append(button)
    return {'type': 'minecraft:multi_action', 'title': {'text': title, 'color': color, 'bold': True},
            'external_title': {'text': title}, 'pause': False, 'body': _page(npc, paragraphs), 'columns': 1,
            'actions': actions, 'exit_action': {'label': {'text': leave}, 'width': 250}}


# An NPC opens <name>, or the furthest-along version that exists: <name>.warden once the Warden is beaten,
# <name>.slain once the Death King is dead, <name>.end1/2/3 once the throne is decided (see Director.dialogFor).
FERRY_ASK = [("Who are you?", 'ferryman_who'), ("What is this place?", 'ferryman_where'),
             ("What happened to the river?", 'ferryman_river'), ("Who else came down here alive?", 'ferryman_others')]
FERRY_SHOP = [("Do you have my things?", 'custom:reclaim'), ("Show me what you sell.", 'custom:trade'), ("Can I do anything for you?", 'custom:quest/ferryman')]
ALDOUS_SHOP = [("Can you make my armor stronger?", 'gravedigger_forge'), ("Let's trade.", 'custom:trade'), ("About Mira...", 'custom:quest/gravedigger')]

DIALOGS = {
    # ------------------------------------------------------------------ the Ferryman
    'ferryman': _dialog('ferryman', [
        "You're wearing a chain. I know that rattle. I've heard it before.",
        "You're not dead. You came down here on purpose. They always do.",
    ], FERRY_ASK + FERRY_SHOP),
    'ferryman_who': _dialog('ferryman', [
        "I'm the Ferryman. When people died, I rowed their souls down the River of the Dead to the Warden at the Great Door.",
        "He judged them. The good went through the Door, into the Lake of Souls, to rest. That was the order of things.",
        "*Now the river is dust and I still hold the oars. Habit.",
    ], [("Back", 'ferryman')]),
    'ferryman_where': _dialog('ferryman', [
        "This is the old landing. The river ran right here. Now it's a ditch full of bones.",
        "The bridges meet at the Crossing. West is the Ghostwood. East is Lantern's End, where Aldous lives. "
        "North-west of the Crossing is the Crypt. Something hangs in chains down there.",
        "North is the Great Door. Past it, the Citadel, where the Lake used to be.",
        "*If you die down here, I'll keep your things. Come back and ask.",
    ], [("Back", 'ferryman')]),
    'ferryman_river': _dialog('ferryman', [
        "A living man came down, like you. He killed the King and sat on the throne. The Warden knelt to him.",
        "He shut the Great Door. Then he started eating the souls in the line. Then he drank from the Lake of Souls, "
        "every night, until there was nothing left.",
        "No Lake, no river. No river, and the land cracks into pieces and floats away. That's what you're standing on.",
        "*He calls himself the Death King. He picked the name himself.",
    ], [("Back", 'ferryman')]),
    'ferryman_others': _dialog('ferryman', [
        "You want the truth? I rowed him across. He paid me with a coin from up top. I took it. "
        "That's the coin on my left eye.",
        "I rowed the King down too, long before that. He was alive once, like you. So was the one before him.",
        "*Every one of them said they'd go home afterwards. Not one of them did.",
    ], [("Back", 'ferryman')]),
    'ferryman.warden': _dialog('ferryman', [
        "The Warden knelt to you? Then remember this: he kneels to whoever sits on that throne. Always has.",
        "Be careful what you sit on, bearer.",
        "*His gate won't open for you, by the way. He locked it. Ask the blind man how. He knows everything he can't see.",
    ], [("What happens if I win?", 'ferryman_win')] + FERRY_ASK + FERRY_SHOP),
    'ferryman_win': _dialog('ferryman', [
        "Then the throne is empty. And this place hates an empty throne.",
        "*Something will want you to fill it. Everything down here will.",
    ], [("Back", 'ferryman.warden')]),
    'ferryman.slain': _dialog('ferryman', [
        "It's quiet. Even the line stopped whispering. He's gone, isn't he?",
        "Then the throne is empty, and every soul down here is waiting to see what you do with it.",
        "*Take his Heart to the throne. You'll know what to do. Or you won't. Nobody ever does.",
    ], FERRY_SHOP),
    'ferryman.end1': _dialog('ferryman', [
        "Your Majesty. Forgive me if I don't bow. I've bowed to four of you.",
        "*They all said they'd go home, too.",
    ], FERRY_SHOP),
    'ferryman.end2': _dialog('ferryman', [
        "The old King is back on his chair. He sent word: he wants my oars wet by morning.",
        "The order of things. I'd forgotten how heavy it was.",
        "*He's already asking where the other chains went.",
    ], FERRY_SHOP),
    'ferryman.end3': _dialog('ferryman', [
        "No King. I never thought I'd see it. Nobody judging, nobody ruling. Just a door, and people walking through it.",
        "*Listen. Hear that, under the bones? Water.",
    ], FERRY_SHOP),
    # ------------------------------------------------------------------ Aldous, the Traveler
    'gravedigger': _dialog('gravedigger', [
        "Stay back! ...Wait. You're warm. You're alive, like me.",
        "I'm Aldous. I came down here looking for my daughter. I've been looking a long time.",
        "I was a smith up top. Bring me souls and grave runes and I'll make your armor stronger. Or we can trade.",
    ], [("Why are you down here?", 'gravedigger_why'), ("Where did you get your chain?", 'gravedigger_chain'),
        ("What happened to this village?", 'gravedigger_village')] + ALDOUS_SHOP),
    'gravedigger_forge': _dialog('gravedigger', [
        "Bring me grave runes and souls, and I'll beat the strength of the dead into what you're wearing.",
        "Iron stays iron, but it gets tougher, it glows with the runes, and everything down here hits you softer. "
        "Hover over a piece to see what it gives. Wear it while I work.",
        "*Blades aren't mine down here. There's a little bone golem out at the Mere, south-east of the Landing. Clatter. "
        "He dips them in the water and they come out meaner.",
    ], [("Helm  (1 rune, 10 souls)", 'custom:soulforge/head', "+1 armor, +1 toughness, Underworld hits 4% softer"),
        ("Chestplate  (2 runes, 22 souls)", 'custom:soulforge/chest', "+2 armor, +2 toughness, Underworld hits 8% softer"),
        ("Leggings  (2 runes, 16 souls)", 'custom:soulforge/legs', "+2 armor, +1 toughness, Underworld hits 6% softer"),
        ("Boots  (1 rune, 10 souls)", 'custom:soulforge/feet', "+1 armor, +1 toughness, Underworld hits 4% softer"),
        ("Back", 'gravedigger')]),
    'gravedigger_why': _dialog('gravedigger', [
        "My daughter Mira died of a fever last spring. She was nine. She was missing a front tooth.",
        "I came down to find her. She's somewhere in the line at the Great Door, but there are thousands of faces.",
        "*I know what happens if the Door opens. She goes through, and I never see her again. I want to find her first.",
    ], [("Back", 'gravedigger')]),
    'gravedigger_chain': _dialog('gravedigger', [
        "A blind beggar sold it to me, a week after we buried her. He said her name. I never told him her name.",
        "*I didn't ask how. I should have asked how.",
    ], [("Back", 'gravedigger')]),
    'gravedigger_village': _dialog('gravedigger', [
        "This is Lantern's End. These people died in their sleep and never noticed. They kept sweeping and cooking.",
        "But the line never moved, and nobody came for them. After a while, they just... sat down.",
        "*I dig graves for them. It keeps my hands busy. Some of them still twitch when you walk past. Don't stare.",
    ], [("Back", 'gravedigger')]),
    'gravedigger.warden': _dialog('gravedigger', [
        "You beat the Warden? Then the Door... Did you see her? Brown hair. Missing a tooth.",
        "The blind man came to me in a dream last night. He said if you fail, I should take the throne. "
        "He said a King can send a soul home.",
        "*Could I? ...Don't answer that.",
        "If you're after the bell at Lantern's End: I put a ladder up the tower. Nobody's rung it in years. Nobody living.",
    ], ALDOUS_SHOP),
    'gravedigger.slain': _dialog('gravedigger', [
        "It's over? Then the throne is empty. The blind man says whoever sits there can do anything. Even bring her back.",
        "*Don't let it be me. I'd do it. You know I would.",
    ], ALDOUS_SHOP),
    'gravedigger.end1': _dialog('gravedigger', [
        "You're the King now. You could send her home, if you wanted. ...You won't, will you.",
        "*No. The dead move on. I know. I knew before I came down. I just didn't want to know.",
    ], ALDOUS_SHOP),
    'gravedigger.end2': _dialog('gravedigger', [
        "The line is moving. She went through the Door this morning. I was there. I think she waved.",
        "*The King smiled at me when she passed. I didn't like his smile.",
    ], ALDOUS_SHOP),
    'gravedigger.end3': _dialog('gravedigger', [
        "The Door's open and nobody's guarding it. I saw her go through. She didn't look back.",
        "*That's good. That's how it should be. I'm going home now. Thank you.",
    ], ALDOUS_SHOP),
    # ------------------------------------------------------------------ the Prophet (the old King)
    'prophet': _dialog('prophet', [
        "The chain. I can hear it. Good. You came.",
        "I served the King. When the Death King took the throne, he put out my eyes and hung me here, "
        "so I could never tell anyone what I saw.",
        "*Ask, and I'll tell you what I can.",
    ], [("What did you see?", 'prophet_saw'), ("How do I get past the Warden?", 'prophet_guard'),
        ("How do I beat the Death King?", 'prophet_death'), ("Is there a girl named Mira here?", 'prophet_mira')]),
    'prophet_saw': _dialog('prophet', [
        "A living man walked up the steps to the throne. The King stood up to meet him. He wasn't afraid.",
        "The man killed him. And the Warden knelt to the man, as if he'd been waiting for him.",
        "*The Warden didn't betray anyone. He serves the throne, whoever sits on it. That is his oath.",
    ], [("Back", 'prophet')]),
    'prophet_guard': _dialog('prophet', [
        "The Warden can't stop. He swore to guard the throne's Door, and the oath doesn't care who's on the throne.",
        "When his blade swings wide, get in close. When he lifts it high, back away. After he lunges he's slow to "
        "turn, so hit his back. When he drives his blade into the ground, move sideways.",
        "*Beat him, and the Door will open for you.",
    ], [("Back", 'prophet')]),
    'prophet_death': _dialog('prophet', [
        "The Death King has three forms. You have to beat all three.",
        "First, on the throne. He won't get up. Four anchors feed him souls. Break them.",
        "Second, the Reaper. He vanishes and comes at you from behind. Hit him when he appears.",
        "*Last, the Beast: every soul he ever ate, all at once. Let it charge past you, then hit it as it turns.",
    ], [("Back", 'prophet')]),
    'prophet_mira': _dialog('prophet', [
        "Yes. A little girl near the front of the line. The Warden judged her good. She's waiting for the Door.",
        "*Don't tell her father. A man who wants something that badly is useful. To someone.",
    ], [("Back", 'prophet')]),
    'prophet.warden': _dialog('prophet', [
        "You beat the Warden. Remember: the moment someone sits on that throne, he serves them again.",
        "The Death King is afraid of you. He melted every chain he could find. Every one but one.",
        "*Yours. Ask yourself how it found you.",
    ], [("His gate is sealed.", 'prophet_seals'), ("How did it find me?", 'prophet_found'), ("How do I beat the Death King?", 'prophet_death')]),
    'prophet_seals': _dialog('prophet', [
        "Of course it is. He locked himself in. Three locks, and he hid the keys where he thought nobody would look.",
        "One is with the Kings who came before him. In the Tomb of Kings. Light their way, in the order they ruled.",
        "One is at the top of the Spire, where the skulls keep watch.",
        "One is in the bell at Lantern's End. Ring it for the Kings. All of them. Even him.",
        "*He thought nobody would ever come looking. Nobody ever does, down here.",
    ], [("Back", 'prophet.warden')]),
    'prophet_found': _dialog('prophet', [
        "Chains find the people who need them. Or the people they're sent to.",
        "*That's all you need to know for now. Go.",
    ], [("Back", 'prophet.warden')]),
    'prophet.slain': _dialog('king', [
        "He's dead. I heard his heart stop from here.",
        "Now listen. I didn't serve the King. I am the King. He couldn't kill me. Nobody can kill the dead. "
        "So he took my eyes and hung me with the rest.",
        "*Take his Heart to the empty throne, and set me free. I'll open the Door. I'll put everything back.",
    ], [("You sent my chain.", 'king_chain'), ("Why should I trust you?", 'king_trust')]),
    'king_chain': _dialog('king', [
        "Yes. And the one around Aldous's neck, in case you failed. Someone had to end him. I chose you.",
        "*You're welcome.",
    ], [("Back", 'prophet.slain')]),
    'king_trust': _dialog('king', [
        "You shouldn't. But I'm the only one down here who's done the job before.",
        "*I kept the order for a thousand years. The dead worked, the dead were judged, the dead rested. "
        "Was that so terrible?",
    ], [("Back", 'prophet.slain')]),
    'prophet.end1': _dialog('king', [
        "Thief. You sat in my chair.",
        "*Enjoy it. One day another chain finds another neck. I'll be here. I'm always here.",
    ], [("Goodbye.", 'custom:bye')]),
    'prophet.end2': _dialog('king', [
        "My throne. My order. Thank you, bearer. You paid well.",
        "Go home? With what? You have no chain now. You'll stay, and work, and rest when it's your turn. Like everyone.",
        "*Chains are dangerous things. Someone has to keep them safe. Someone has to keep them... close.",
    ], [("You've changed.", 'king_changed'), ("Goodbye.", 'custom:bye')]),
    'king_changed': _dialog('king', [
        "Have I? I can hear the whole Underworld from this chair. Every soul in the line. Every one that ever got away.",
        "Silas heard it too, at the end. He said it sounded like hunger. I always thought he was weak.",
        "*Go. Before I start listening to you.",
    ], [("Back", 'prophet.end2')]),
    'prophet.end3': _dialog('king', [
        "You broke it. There's no throne. No King. Nobody to keep the order.",
        "*Maybe that's what the Hunter knew, all those years ago. Leave me. I want to listen to them walk through.",
    ], [("Goodbye.", 'custom:bye')]),
    # ------------------------------------------------------------------ the Collector
    'collector': _dialog('collector', [
        "Mind the floor. Ah, you found it already. Good.",
        "You're warm. A living thing with a chain, down here. How rare. How very collectable.",
        "*I don't want your soul. I want your pockets.",
    ], [("Who are you?", 'collector_who'), ("What do you collect?", 'collector_things'),
        ("What do you know about the Kings?", 'collector_kings'), ("Is there anything out west?", 'collector_west'),
        ("I found something for you.", 'custom:collect'), ("Show me what you sell.", 'custom:trade')]),
    'collector_who': _dialog('collector', [
        "Nobody. A collector. I keep what the Underworld forgets, so that something remembers it.",
        "Every King thinks he's the first. Every King is wrong. I have their things to prove it.",
        "*How long have I been down here? Longer than the King. Not as long as the Hunter.",
    ], [("Back", 'collector')]),
    'collector_things': _dialog('collector', [
        "Forgotten things. A coin, a toy, a ring, a quill. Things with a story stuck to them.",
        "There are eight I want. Bring them to me and I'll pay you well: souls, runes, brews. Something better if you bring all eight.",
        "*Where are they? Where things are forgotten. Boats. Tombs. Bosses. The bottom of a lake.",
    ], [("Back", 'collector')]),
    'collector_kings': _dialog('collector', [
        "Halden dug down from up top and took the throne with a pickaxe. Ysolde shot him off it. "
        "The King strangled Ysolde with her own bowstring. And then a boy called Silas.",
        "Every one of them came down alive. Every one of them meant to go home.",
        "*Look at my crowns. Then ask yourself why there's never a crown for anyone who left.",
    ], [("Back", 'collector')]),
    'collector_west': _dialog('collector', [
        "West? Past the Watch? No. Nobody goes west.",
        "He was the first, you know. Before the King, before Halden. The very first living soul to come down. "
        "They offered him the throne.",
        "*He said no. He wanted something better than a throne. He wanted prey.",
    ], [("Back", 'collector')]),
    # ------------------------------------------------------------------ Clatter, the Bonesmith (he can't talk: he clacks, and mimes)
    'bonesmith': _dialog('bonesmith', [
        "*Clack. Clack-clack.*",
        "A golem of stacked bones and grave-stone, no taller than you, with a head like a box of skulls. He looks at your "
        "weapon. Then at the black water. Then back at your weapon.",
        "*He holds out a hand the size of a shovel.*",
    ], [("What is this place?", 'bonesmith_mere'), ("Who made you?", 'bonesmith_who'), ("Temper my weapon.", 'bonesmith_temper')]),
    'bonesmith_mere': _dialog('bonesmith', [
        "*He points at the water. Then far off, toward the Citadel. He mimes drinking, a lot, and wipes his jaw.*",
        "The Death King drank the River and the Lake of Souls dry. He never noticed the Mere. It was too small.",
        "*He dips one finger in. Little lights rise where he touches it. Whatever sank in here, the water still remembers it.*",
    ], [("Back", 'bonesmith')]),
    'bonesmith_who': _dialog('bonesmith', [
        "*He pats his chest. Then he draws a bow in the air, and puts a crown on his head with both hands.*",
        "Ysolde, the second King, built him out of the bones of the first smiths. She wanted a smith who would never leave.",
        "*He looks at the bridge to the Landing for a long time. He has never crossed it.*",
    ], [("Back", 'bonesmith')]),
    'bonesmith_temper': _dialog('bonesmith', [
        "*He takes your weapon, plunges it into the Mere, and holds it there while the water hisses. Then he holds up fingers: one, two, three.*",
        "Three quenches, each one after the last. Each costs Grave Runes and souls. He won't do the second until the Door's guard "
        "has knelt to you, or the third while the Hunter still lives.",
        "*Hold the weapon you want tempered. Hover over a quench to see what it does.*",
    ], [("I: Mere-Tempered  (1 rune, 12 souls)", 'custom:temper/1', "+1.5 attack damage."),
        ("II: Soul-Quenched  (2 runes, 20 souls)", 'custom:temper/2',
         "+3 attack damage. Your hits light up the dead and drag at them. Needs the Warden beaten."),
        ("III: Deathbound  (3 runes, 32 souls)", 'custom:temper/3',
         "+4.5 attack damage. Every dead thing you put down gives you back a little life. Needs the Hollow Hunter dead."),
        ("Back", 'bonesmith')]),
    # ------------------------------------------------------------------ Mira, in the line at the Door
    'mira': _dialog('mira', [
        "Are you here for the line? It's very long. I'm counting. I got to four hundred and then a lady sneezed and I lost it.",
        "*She's small, and see-through, and missing a front tooth. She smiles with the gap anyway.*",
    ], [("Who are you?", 'mira_who'), ("Your father sent me.", 'custom:quest/mira')]),
    'mira_who': _dialog('mira', [
        "I'm Mira. I'm nine. I'll be nine forever now, which is fine, nine is the best one.",
        "The soldier at the door says I'm good. So I just have to wait until it opens.",
        "*Don't tell Papa I'm here. He'd try to come and get me, and he's not supposed to be down here. He's alive.",
    ], [("Back", 'mira')]),
    'quest.mira.alone': _dialog('mira', [
        "Papa? Papa's not here. Papa's up top, with the bread and the forge and the dog.",
        "*She doesn't sound sure.*",
    ], [("Back", 'mira')]),
    'quest.mira.met': _dialog('mira', [
        "That's Papa's lantern. He leaves it lit on the windowsill so I can find the house in the dark.",
        "*She holds it for a long time. Then she unties the ribbon from her braid and puts it in your hand.*",
        "*Give him this. So he knows I'm not scared. I'm not, really. Tell him to go home.",
    ], [("I'll give it to him.", 'mira')]),
    'quest.mira.met_after': _dialog('mira', [
        "Did he cry? He cries at everything. He cried at a cat once.",
    ], [("Back", 'mira')]),
    'quest.mira.offer': _dialog('gravedigger', [
        "She's in that line at the Door. My Mira. I know it. But I can't get near it, not with the Warden standing there.",
        "You could. You're braver than me, or you're stupider, and down here it's the same thing.",
        "*Take her my lantern. She hated the dark. Then come back and tell me she's all right.",
    ], [("I'll take it to her.", 'custom:quest_accept/mira'), ("Not now.", 'gravedigger')]),
    'quest.mira.wait': _dialog('gravedigger', [
        "Did you find her? Near the back of the line at the Great Door. Small. Missing a front tooth. She'll be counting something.",
    ], [("Back", 'gravedigger')]),
    'quest.mira.wait2': _dialog('gravedigger', [
        "You saw her? Then where's - did she give you anything? She always gave you something.",
    ], [("Back", 'gravedigger')]),
    'quest.mira.done': _dialog('gravedigger', [
        "Her ribbon. She tied this round my wrist every morning so I wouldn't forget her at the forge. As if I could.",
        "She said go home? ...She would say that.",
        "*He's quiet for a while. Then he presses something into your hand: runes, and souls. \"I was saving them for her. She won't need them.\"",
    ], [("Back", 'gravedigger')]),
    'quest.mira.after': _dialog('gravedigger', [
        "She's in the line. She's all right. That's enough for me. That has to be enough.",
    ], [("Back", 'gravedigger')]),
    # the Ferryman's oar
    'quest.oar.offer': _dialog('ferryman', [
        "There's something you could do. My oar. The good one, the one that knew the river.",
        "When the water went, I put her in the boat-shed at the south of the Ghostwood. The wood took the shed. I don't go in there.",
        "*Bring her back to me, and I'll give you something that's worth more than a trip across.",
    ], [("I'll find it.", 'custom:quest_accept/oar'), ("Not now.", 'ferryman')]),
    'quest.oar.wait': _dialog('ferryman', [
        "The boat-shed. South edge of the Ghostwood, under the pale trees. Mind what moves in there.",
    ], [("Back", 'ferryman')]),
    'quest.oar.done': _dialog('ferryman', [
        "*He takes the oar like you'd take a hand. He runs his thumb along the grain.* There you are.",
        "Here. A Ferryman's Charm. It'll take you up out of here and back down again, a few times. Don't waste it.",
    ], [("Back", 'ferryman')]),
    'quest.oar.after': _dialog('ferryman', [
        "*The oar leans against his post. Now and then he touches it, as if checking it's still there.*",
    ], [("Back", 'ferryman')]),
    # the Lamplighter
    'lamplighter': _dialog('lamplighter', [
        "Evening. It's always evening. I light the lamps of Lantern's End, so the dead can find their way home.",
        "*He lifts his lamp to look at you, the way you'd look at a lamp that had come on by itself.*",
    ], [("Who are you?", 'lamplighter_who'), ("Can I help you with anything?", 'custom:quest/lamplighter')]),
    'lamplighter_who': _dialog('lamplighter', [
        "I lit lamps up top, for forty years. When I died I came down and there were lamps here too, going out. So.",
        "*Nobody asked me. Nobody needed to.",
    ], [("Back", 'lamplighter')]),
    'quest.lamps.offer': _dialog('lamplighter', [
        "The souls are going out of the lamps. The Death King's been eating them, the way he eats everything.",
        "Bring me three jars with a soul in each. The Collector sells them, or you can bottle your own: a jar and a soul.",
        "*Do that, and I'll give you a little light of your own. One that follows.",
    ], [("I'll bring them.", 'custom:quest_accept/lamps'), ("Not now.", 'lamplighter')]),
    'quest.lamps.wait': _dialog('lamplighter', [
        "Three soul jars. The lamps can wait a little longer. They've waited a long time already.",
    ], [("Back", 'lamplighter')]),
    'quest.lamps.done': _dialog('lamplighter', [
        "*He tips the souls into his lamp one at a time. The street brightens, a little.*",
        "And this is for you. A wisp of my own lamp, in a lantern of its own. Hold it up and it'll wake. It'll warn you when the dead come close.",
    ], [("Back", 'lamplighter')]),
    'quest.lamps.after': _dialog('lamplighter', [
        "Is it keeping you company? It likes you. They usually do, the ones who bring them home.",
    ], [("Back", 'lamplighter')]),
    # the children
    'kid': _dialog('kid', [
        "Are you ALIVE? You're so alive. You're all warm and pink. Ew.",
        "We're playing Judgement. Tib's the Warden and everyone else is the line, and if you get judged you have to go in the fountain.",
    ], [("Who are you?", 'kid_who'), ("Can I help with anything?", 'custom:quest/kid')]),
    'kid_who': _dialog('kid', [
        "I'm Pip. That's Tib, and Nell, and the little one doesn't have a name yet. He's still deciding.",
        "*We used to have a ball. We had the best ball.",
    ], [("Back", 'kid')]),
    'quest.ball.offer': _dialog('kid', [
        "Tib kicked our ball off the bridge. Really hard. It went all the way to the Crossing, by the old footbridge, in the dry river.",
        "We're not allowed to go to the Crossing. Things eat you at the Crossing.",
        "*Can you get it? Please? I'll give you my best thing.",
    ], [("I'll get your ball.", 'custom:quest_accept/ball'), ("Not now.", 'kid')]),
    'quest.ball.wait': _dialog('kid', [
        "It's under the footbridge at the Crossing! In the dry river! Tib says it's gone forever but Tib's stupid.",
    ], [("Back", 'kid')]),
    'quest.ball.done': _dialog('kid', [
        "OUR BALL! Tib, TIB, it's our BALL!",
        "*Pip gives you their best thing: a Grave Rune, a bit chewed, and a handful of souls.* We found them. Don't ask where.",
    ], [("Back", 'kid')]),
    'quest.ball.after': _dialog('kid', [
        "You can play if you want. You'd have to be the line. Everyone new has to be the line.",
    ], [("Back", 'kid')]),
    # the Sentry
    'sentry': _dialog('sentry', [
        "Halt. Who goes. ...Sorry. Habit. Nobody's come this way in a long time.",
        "*A soldier's ghost, in a dented helm, facing west across the gap. He never turns his back on it.*",
    ], [("What are you guarding?", 'sentry_guard'), ("Is something wrong?", 'custom:quest/sentry')]),
    'sentry_guard': _dialog('sentry', [
        "The Watch. The west. What's out west is the Hollow, and what's in the Hollow is him. The Hunter.",
        "*I was posted here to warn the King if he ever came east. I've been waiting a very long time. He never comes. That's worse.",
    ], [("Back", 'sentry')]),
    'quest.name.offer': _dialog('sentry', [
        "I can't remember my name. Isn't that stupid? I remember my post, my orders, my sergeant's face. Not my name.",
        "It's on my tag. I went out across the broken bridge once, toward the Hollow. Something moved out there and I ran. I dropped it halfway.",
        "*If you're going that way anyway. If you're mad enough. Bring it back?",
    ], [("I'll bring your tag back.", 'custom:quest_accept/name'), ("Not now.", 'sentry')]),
    'quest.name.wait': _dialog('sentry', [
        "Halfway across the broken bridge to the Hollow. Mind the gaps. And mind what's watching you from the trees.",
    ], [("Back", 'sentry')]),
    'quest.name.done': _dialog('sentry', [
        "*He holds the tag up to his empty eyes.* Corwin. I'm Corwin. Private Corwin, of the Watch.",
        "Thank you. Here: runes, souls, whatever I'd been keeping for the King. And this, for free: the Hunter can't see you if you stand still in his blind. He hunts what runs.",
    ], [("Back", 'sentry')]),
    'quest.name.after': _dialog('sentry', [
        "Corwin. Still at my post, Corwin is. Still watching west.",
    ], [("Back", 'sentry')]),
    # ------------------------------------------------------------------ the empty throne (opened by the Heart)
    'throne': _dialog('throne', [
        "The throne is empty. The Heart beats in your hand, slow and heavy.",
        "The Warden is waiting at the foot of the steps. Somewhere below, a blind man is listening.",
        "*Whatever you choose, the Underworld will remember it.",
    ], [("Sit on the throne.", 'custom:ending/take', "Become the next King. The Warden serves you. The cycle goes on."),
        ("Free the old King.", 'custom:ending/king', "Give the throne back to the King. Order returns. He will want paying."),
        ("Break the throne.", 'custom:ending/break', "No more Kings. Nothing holds the Underworld together after that. Run.")],
        leave='Not yet'),
}

# ---------------------------------------------------------------------------------------------------- lore books
BOOKS = {
    'arrival': ("To Whoever Wears It Next", "R.", [
        "If you're reading this, a chain found you too. Mine did. I never found out who sent it.",
        "The Ferryman trades for souls, not gold. If you die, he keeps your things. Go back and ask him.\n\n"
        "Aldous, in the east village, is alive. Help him.",
        "There's a King on the throne who isn't a King. I'm going to go and see him.\n\nIf you find my bones, bury them.",
    ]),
    'village': ("Papa", "Mira", [
        "Papa,\n\nThe soldier at the door says I am good. But the door is shut, so I have to wait in line.",
        "Don't hurry. I made a friend. She has no face but she is nice. We count the people in the line. "
        "It's a very big number.\n\nLove,\nMira",
    ]),
    'crypt': ("The Rule of the Throne", "Carved over the tombs", [
        "The dead cannot rule the dead. Only a living soul may sit on the throne.",
        "When a living soul comes down wearing a chain, and takes the throne by strength, the Warden kneels, "
        "and the old King steps down.",
        "So it was with Halden. So it was with Ysolde. So it was with the King.",
        "(Scratched underneath, fresh:)\n\nAnd so it will NOT be with me.\n\n- S.",
    ]),
    'ruins': ("The Warden's Oath", "The Warden", [
        "I will judge every soul fairly, rich or poor.\n\nI will guard the Door, and I will serve the throne, "
        "whoever sits on it.",
        "Sworn on the first day.",
        "(Scratched underneath, much later:)\n\nI know what he is. I serve him anyway. That is what an oath is.",
    ]),
    'spire': ("The Three Forms", "Written for the King", [
        "ON THE THRONE\n\nHe won't stand up. Four anchors feed him souls. Break them all.",
        "THE REAPER\n\nHe vanishes and strikes from behind. Wait for him to appear, then hit him.",
        "THE BEAST\n\nEvery soul he ate, wearing one body. Let it charge past you, then strike as it turns.",
    ]),
    'watch': ("Watch Log", "The Western Sentry", [
        "Day 1. The line is long. All quiet.\n\nDay 9. The line hasn't moved since the bell stopped.\n\n"
        "Day 40. Still hasn't moved.",
        "Day ??. Saw a light far to the west, past the edge. Something out there with one eye, watching me back.",
        "Day ??. The eye is closer. I'm going down to join the line. They say it's warm in the line.",
    ]),
    'hollow_root': ("The Last Tree", "A forester", [
        "When the river still ran, the dead planted this forest so the waiting would have some shade.",
        "When the river dried up, the trees stopped growing. They didn't die. Nothing down here can.",
        "I hid my treasure under the oldest root, behind a wall that isn't really a wall. If you found this, it's yours.",
    ]),
    'citadel': ("The Death King's Ledger", "The Death King", [
        "CHAINS\n\nFound: 11\nMelted: 10\nMissing: 1",
        "SOULS\n\nEaten: I stopped counting.\n\nLAKE\n\nLeft: a few cups. Tomorrow, none.",
        "(Pressed so hard the page tore:)\n\nNOBODY TAKES IT FROM ME.",
    ]),
    'apothecary': ("Brews for Below", "The Apothecary of Lantern's End", [
        "Start from an awkward brew, as always.\n\nA soul: WARDING. The Underworld's blows land softer.",
        "A jar of eyes: GRAVE SIGHT. Everything that hunts you, you see first, through stone.",
        "A grave rune: LAST BREATH. Death comes for you once, and misses.\n\nRedstone makes the first two last longer.",
    ]),
    'hunted': ("Don't Go West", "A pilgrim, last pages", [
        "Crossed the broken bridge from the Watch. Nobody stopped me. Now I know why.",
        "Something up in the bone trees, with one eye. It doesn't chase. It doesn't need to. It just waits for you to move.",
        "The Collector says it was the first. The very first living soul to come down. It never wanted the throne. "
        "It wanted this.",
    ]),
    # the Death King's own log, a page on each island, in the order he walked them
    'silas1': ("Log, Page 1", "Silas", [
        "Found a chain at the bottom of a stronghold. Skull on it, cold as ice. Everyone says the Underworld is a myth.",
        "Tomorrow I find out. If I die for real... whatever. I've died a hundred times. You just respawn.",
    ]),
    'silas2': ("Log, Page 2", "Silas", [
        "It's real. The Ferryman took my coin. The dead stand in a line a mile long and nobody fights back.",
        "This place is a game nobody's playing. I'm going to beat it.",
    ]),
    'silas3': ("Log, Page 3", "Silas", [
        "Met the King. He knew why I came before I said a word.",
        "He said: \"The throne goes to whoever takes it. That is the rule. Take it, if you can.\"\n\nHe wasn't even scared.",
    ]),
    'silas4': ("Log, Page 4", "Silas", [
        "I did it. The Warden knelt. The line bowed. I'm the King.",
        "Then the old man laughed, with his own blood on his face, and told me the rest of the rule. "
        "One day another chain comes down. And it takes the throne from me.",
    ]),
    'silas5': ("Log, Page 5", "Silas", [
        "Shut the Door. No souls through, nobody else gets strong. Only me.",
        "Ate one today. It tasted like a summer I never had. Ate ten more.",
    ]),
    'silas6': ("Log, Page 6", "Silas", [
        "Melted every chain I could find. Ten of them. There's one more up there. I can feel it moving.",
        "Drank from the Lake again. It's nearly dry. Good. Let it be dry.",
    ]),
    'silas7': ("Log, Last Page", "The Death King", [
        "I can't remember my face up there. Or my real name. Silas? It sounds like someone else.",
        "Doesn't matter. I won. I'm going to keep winning. Forever.",
    ]),
}

def book_pool(name, chance=1.0):
    title, author, pages = BOOKS[name]
    p = {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:written_book', 'functions': [
        {'function': 'minecraft:set_book_cover', 'title': {'raw': title}, 'author': author, 'generation': 3},
        {'function': 'minecraft:set_written_book_pages', 'mode': 'replace_all',
         'pages': [{'raw': {'text': t, 'color': '#2a1f33'}} for t in pages]}]}]}
    if chance < 1.0:
        p['condition'] = {'type': 'minecraft:random_chance', 'chance': chance}
    return p


# ---------------------------------------------------------------------------------------------------- journals of the dead
# Last pages of people who died up top, any way the world kills you. Their souls are in the line now.
JOURNALS = {
    'pyramid': ("Sand Keeps Time", "Talia, treasure hunter", [
        "Day 1. A temple in the desert. Blue clay in the floor, shaped like an eye. I knew the treasure was under it.",
        "I broke the eye and fell. The treasure was there. So was the dark. The walls were too smooth to climb.",
        "Day 6. Gold and diamonds, and no bread. I'd trade every gem for one loaf.",
        "Sand trickles from the cracks, slow as a clock. I think it's counting for me.",
    ]),
    'wither': ("Three Skulls", "Corin", [
        "Three skulls. Four blocks of soul sand. I set the last skull, and the sky went black.",
        "It had three heads, and all of them hated me. My sword was a twig. My armor was paper.",
        "I thought I was a hero. I was only the one who woke it. I'm sorry, everyone who lived nearby.",
    ]),
    'lava': ("Bait", "Edda, miner", [
        "Diamonds! Four of them, shining at the bottom of the cave. I swung my pick, and the floor gave way.",
        "It wasn't water underneath. It was orange, and loud, and very warm.",
        "If you see diamonds next to lava, leave them. They're bait. They were always bait.",
    ]),
    'warden': ("Something Listening", "Pell", [
        "Deep down, the stone turns blue and breathes. Something under it was listening.",
        "I crept. I crouched. I held my breath. Then a little shrieker screamed, and the ground began to shake.",
        "It had no eyes. It didn't need them. It heard my heart, and followed the sound.",
    ]),
    'creeper': ("A Soft Hiss", "Bram, farmer", [
        "I built my house with my own hands. Oak walls, a glass window, a field of wheat.",
        "I was looking at my wheat when I heard it. A soft little hiss, right behind me.",
        "I never turned around. I'm glad. I don't think I wanted to see it smile.",
    ]),
    'phantom': ("Go To Bed", "Lio", [
        "Three nights without sleep. There was always one more tunnel, one more ore.",
        "On the fourth night the sky grew wings. They screamed as they dove, blue and torn and endless.",
        "If you're reading this, go to bed. Please. Just go to bed.",
    ]),
    'void': ("Under Me, Stars", "Saro", [
        "I beat the dragon. I stood at the edge of the world and laughed.",
        "An enderman stared at me, and I stepped back to get a better look.",
        "There was no ground. Only stars, above me and below me, and a long, quiet fall. It was almost pretty.",
    ]),
    'piglin': ("Wear The Gold", "Fenna", [
        "Note to self: the pig folk like gold. I left my gold boots by the portal.",
        "They did not like that. There were very many of them, and they were very fast.",
        "Wear the gold. Even if it looks silly. Especially then.",
    ]),
    'bed': ("A Bed In The Nether", "Ivo", [
        "It was late, and home was far. I thought: I'll just sleep here, in my red bed.",
        "The bed did not agree.",
        "My last thought was how pretty the blast looked against the netherrack. Silly, what you think about.",
    ]),
    'monument': ("Green Light", "Marin, diver", [
        "The sea temple glowed green under the waves. I took a deep breath and swam down.",
        "A great fish watched me with its one eye. My arms grew heavy. My breath ran out.",
        "The light from the surface looked so close. It wasn't.",
    ]),
    'raid': ("The Gate", "Hollis", [
        "I drank the dark bottle the captain dropped. I thought it was a prize.",
        "That night the bells rang and the villagers ran. The raiders came with axes and banners.",
        "I held the gate as long as I could. The villagers lived. I didn't. That seems fair.",
    ]),
    'snow': ("So Soft", "Nel", [
        "Fresh snow on the mountain. So soft. So white.",
        "I stepped in and kept sinking. It wasn't snow. It was powder, and it was very cold.",
        "I could see the sun the whole time. It just wasn't warm enough.",
    ]),
    'elytra': ("For A Second", "Aya", [
        "Wings! I found wings on a ship at the end of the world. I flew over oceans and mountains.",
        "I went faster, and faster. The cliff came up faster than that.",
        "For a second, I really was flying.",
    ]),
    'mineshaft': ("Small Blue Spiders", "Tam", [
        "An old mineshaft, full of rails and cobwebs. I followed the rails down.",
        "The webs got thicker. Small blue spiders poured out of a cage. Then more. Then more.",
        "I feel sick and everything looks green. There's a minecart here. I think I'll sit in it a while.",
    ]),
    'old': ("The Cat Is Patient", "Mabel", [
        "I lived a long time in my little house by the river. A cat, a garden, a good view.",
        "One night I lay down and didn't get up. I wasn't scared. I thought I'd see the river again.",
        "But the river is dry and the line is long. I'm still waiting, my cat in my arms. He's patient. So am I.",
    ]),
}


def _book(title, author, pages):
    return {'type': 'minecraft:item', 'name': 'minecraft:written_book', 'functions': [
        {'function': 'minecraft:set_book_cover', 'title': {'raw': title}, 'author': author, 'generation': 3},
        {'function': 'minecraft:set_written_book_pages', 'mode': 'replace_all',
         'pages': [{'raw': {'text': t, 'color': '#2a1f33'}} for t in pages]}]}


def journal_pool(chance=1.0):
    """One random journal of the dead (the deathbound:journal loot table)."""
    p = {'rolls': 1, 'entries': [{'type': 'minecraft:loot_table', 'value': f'{NS}:journal'}]}
    if chance < 1.0:
        p['condition'] = {'type': 'minecraft:random_chance', 'chance': chance}
    return p


# ---------------------------------------------------------------------------------------------------- the line's last words
LOST_SOUL_LINES = [
    "Is it my turn yet?",
    "I left the lamp burning. Someone should blow it out.",
    "He weighed me. He said nothing. He always says nothing.",
    "Don't look at the Door too long. It looks back.",
    "I was a baker. I think I was a baker.",
    "How long have we been walking?",
    "There used to be water here. I remember being wet.",
    "My name... it was right here...",
    "You're warm. Why are you warm?",
    "The ones who fall out of line get up again. Wrong.",
    "Mother promised there would be light.",
    "The Ferryman has my coin. I paid. I paid.",
    "Don't take off the chain. Not here.",
    "He eats one of us every night. You can hear it from the line.",
    "I can't feel my hands. I can't feel your hand.",
    "Kings walk in this line too. They hate it.",
    "Turn back. Or don't. None of us did.",
    "The old King made us work before we could rest. At least we rested.",
    "A little girl asked me if I'd seen her father. I said yes. I lied.",
    "When the Warden looks at you, don't blink.",
    "The Warden cried when he knelt. I saw it.",
    "Another chain? Oh no. Oh, not again.",
    "Whoever sits up there, the line never gets shorter.",
    "I heard a bowstring out past the edge. Nobody goes past the edge.",
]

ITEM_LORE = {
    'soul': ["Still warm. Still whispering."],
    'grave_rune': ["Aldous can forge this."],
}

LINGERING = {
    'arrival': ["The Ferryman told me to wait. That was a long time ago.",
                "You came down on purpose? Why would anyone...",
                "Your heart is beating. It's so loud."],
    'crossing': ["Every bridge comes back here. I've tried them all.",
                 "The river split here once. Left to the trees, right to the lamps.",
                 "The pillars hum when nobody's listening."],
    'ghostwood': ["The trees remember being trees. That's the worst part.",
                  "Something is buried under the old root. Something with a lock.",
                  "Don't rest here. The roots listen."],
    'village': ["Is it supper yet? I set the table. I always set the table.",
                "The bell rang and nobody came.",
                "Aldous digs and digs. Tell him she isn't in the ground."],
    'crypt': ["Shh. The Kings are sleeping. Don't wake the others.",
              "The blind one below talks to himself. He keeps saying 'my throne'.",
              "There's a wall down here that isn't. I walk through it sometimes."],
    'spire': ["We built it to see the far shore. There is no far shore.",
              "The wind up here sounds like my name.",
              "There's a man under the floor. He counts things. He counted me."],
    'watch': ["They all turned and looked up at me. All at once.",
              "What day is it? I keep the log. I keep the log.",
              "Out west, past the edge, something with one eye. Don't go west."],
    'gate': ["He weighed me twice. I don't know what that means.",
             "Stand in line long enough and you forget why.",
             "Near the front of the line there's a little girl who counts us."],
    'citadel': ["This was all water once. We floated. It was so quiet.",
                "He drinks from the Lake every night. There's nothing left to drink.",
                "Four anchors hold him up. Break them and he has to stand."],
}

AREAS = {
    'arrival': ("The Landing", "The Ferryman's old landing."),
    'hub': ("The Crossing", "Where every bridge meets."),
    'forest': ("The Ghostwood", "Nothing grows here anymore."),
    'village': ("Lantern's End", "The lamps are still lit."),
    'crypt': ("The Tomb of Kings", "Almost every King lies here."),
    'spire': ("The Wailing Spire", "It never saw the far shore."),
    'watch': ("The Western Watch", "The sentry is still at his post."),
    'gate': ("The Great Door", "Judged, and still waiting."),
    'citadel': ("The Death King's Citadel", "Built on a lake drunk dry."),
    'hollow': ("The Hollow", "Nothing here is hunted twice."),
    'mere': ("The Mere", "The one water he never drank."),
}

SAY = {
    'warden.start': "Stop. The throne has a King. The living do not pass.",
    'warden.judge': "I judge you!",
    'warden.half': "I serve the throne. Whoever sits on it.",
    'warden.kill': "Judged.",
    'warden.defeat': "Enough... Kill him, and I will serve you.",
    'death.wake': "So the last chain found a neck.",
    'death.rise': "I melted every chain but yours. I should have looked harder.",
    'death.blink': "Behind you.",
    'death.beast': "You want the throne? Look what it made of me!",
    'death.low': "I came down here to win. I won. Why does it feel like this?",
    'death.kill': "Another one for the Lake.",
    'death.defeat': "It'll eat you too... the throne... it eats everyone...",
    'hunter.start': "Another one with a chain. Run. It's better when they run.",
    'hunter.half': "Good. You learn. The ones who learn last longest.",
    'hunter.kill': "Quiet now.",
    'hunter.defeat': "At last... one who came for me, and not for the throne.",
}

LANG = {
    'deathbound.say': '%s: %s',
    'deathbound.speaker.warden': 'The Warden',
    'deathbound.speaker.death': 'The Death King',
    'deathbound.speaker.hunter': 'The Hollow Hunter',
    'entity.deathbound.hollow_hunter': 'The Hollow Hunter',
    'item.deathbound.hollow_hunter_spawn_egg': 'Hollow Hunter Spawn Egg',
    'item.deathbound.hunters_charm': "Hunter's Charm",
    'item.deathbound.hunters_charm.lore': 'The first one down. He never missed.',
    'item.deathbound.hunters_charm.desc': 'Your arrows fly faster',
    'item.deathbound.hunters_charm.desc2': 'and hit half again as hard.',
    'entity.deathbound.deaths_guard': 'The Warden',
    'entity.deathbound.death': 'The Death King',
    'boss.deathbound.death.throne': 'The Death King, Upon the Throne',
    'boss.deathbound.death.reaper': 'The Death King, Reaper',
    'boss.deathbound.death.beast': 'The Death King, Unbound',
    'cinematic.deathbound.death': 'THE DEATH KING',
    'cinematic.deathbound.death.reaper': 'THE DEATH KING RISES',
    'cinematic.deathbound.death.beast': 'THE DEATH KING, UNBOUND',
    'cinematic.deathbound.victory': 'THE DEATH KING HAS FALLEN',
    'cinematic.deathbound.victory.sub': 'Bring his Heart to the empty throne.',
    'cinematic.deathbound.ending.take': 'LONG LIVE THE KING',
    'cinematic.deathbound.ending.take.sub': 'The dead obey you now. Up above, a new chain has started to glow.',
    'cinematic.deathbound.ending.king': 'THE KING RETURNS',
    'cinematic.deathbound.ending.king.sub': 'Order is back, and your chain is his. Something in it has started whispering to him.',
    'cinematic.deathbound.ending.break': 'THE THRONE IS BROKEN',
    'cinematic.deathbound.ending.break.sub': 'No King, no Door, no judge. The dead are free, and what is left down here is wild.',
    'title.deathbound.fall': 'RUN',
    'title.deathbound.fall.sub': 'Get back to the Landing.',   # short: vanilla subtitles run off the screen past ~40 characters
    'bossbar.deathbound.fall': 'The Underworld is falling',
    'message.deathbound.fall.caught': "Something pulls you out of the dark by your collar. The Ferryman doesn't say a word.",
    'effect.deathbound.marked': 'Grave-Marked',
    'message.deathbound.ending.price': 'The King has your chain. Everything it did for you, it does for him now.',
    'title.deathbound.throne_empty': 'THE THRONE IS EMPTY',
    # captions under each ending's shots (see Cutscene.java): 1 take the throne, 2 free the old King, 3 break it
    'cutscene.deathbound.1.0': 'You sit. The throne is cold, and it fits you perfectly.',
    'cutscene.deathbound.1.1': 'The chains find you. They always find the King.',
    'cutscene.deathbound.1.2': 'The Warden kneels. He always kneels to whoever sits there.',
    'cutscene.deathbound.2.0': 'The chains fall from the blind King. His crown was waiting under the throne. He sits.',
    'cutscene.deathbound.2.1': '"Order has a price, bearer. Your chain." Everything it gave you goes out of you, and into him.',
    'cutscene.deathbound.2.2': 'At the Door, the Warden takes up his post again. The line moves. One by one, they are judged.',
    'cutscene.deathbound.2.3': 'And the Lake of Souls begins to fill.',
    'cutscene.deathbound.2.4': 'He turns your chain over and over in his hands. "Mine," he whispers. Not to you.',
    'cutscene.deathbound.3.0': 'The Heart cracks the throne in two.',
    'cutscene.deathbound.3.1': 'The Door comes down. The Warden takes off his helm, and walks away from it.',
    'cutscene.deathbound.4.0': 'Behind you, the Underworld goes down into the dark. The dead go up, as lights.',
    'item.deathbound.spent_chain': 'Spent Chain',
    'item.deathbound.spent_chain.lore0': 'The King kept everything it could do.',
    'item.deathbound.spent_chain.lore1': "It's only a chain now. The only way home is the long one.",
    'title.deathbound.throne_empty.sub': 'Bring his heart to it, and use it on the throne.',
    'item.deathbound.deathbound_relic.lore': "A chain that keeps a soul in its body.",
    'item.deathbound.heart_of_death': "The Death King's Heart",
    'item.deathbound.heart_of_death.lore': 'It still beats. Slowly.',
    'item.deathbound.heart_of_death.desc': 'Bring it to the empty throne, and use it on the throne.',
    'item.deathbound.heart_of_death.desc2': 'Afterwards: awakens a relic.',
    'message.deathbound.heart.throne': 'The Heart pulls toward the Citadel. Bring it to the empty throne.',
    'message.deathbound.ending.done': 'The throne has already been decided.',
    'message.deathbound.ending.take': 'You sit. The throne is cold, and it fits you perfectly.',
    'message.deathbound.ending.king': 'The chains fall from the blind King. He takes his crown back, and sits.',
    'message.deathbound.ending.break': 'The Heart cracks the throne in two. Under your feet, the Underworld starts to come apart.',
    'item.deathbound.open_door_charm': 'Charm of the Open Door',
    'item.deathbound.open_door_charm.lore': 'The Door is open. For you, always.',
    'item.deathbound.open_door_charm.desc': 'Use the relic to cross between',
    'item.deathbound.open_door_charm.desc2': 'the worlds, whenever you like.',
    'block.deathbound.ghostwood_chair': 'Ghostwood Chair',
    'block.deathbound.ghostwood_table': 'Ghostwood Table',
    'block.deathbound.bone_candelabra': 'Bone Candelabra',
    'block.deathbound.tombstone': 'Tombstone',
    'block.deathbound.ossuary_shelf': 'Ossuary Shelf',
    'entity.deathbound.ferryman': 'The Ferryman',
    'item.deathbound.grave_rune': 'Grave Rune',
    'trim_pattern.deathbound.soulforged': 'Soulforged',
    'trim_material.deathbound.soul': 'Soul',
    'message.deathbound.relic.offhand': 'The chain is quiet. Keep it in your off hand... and die.',
    'message.deathbound.soulforge.done': 'Aldous hammers the dead into your %s. It glows with grave-runes.',
    'message.deathbound.soulforge.wanting': "\"Your %s? That's %s rune(s) and %s souls, friend. Come back when you have them.\"",
    'message.deathbound.soulforge.all': "\"That %s is forged already. Nothing more I can do for it.\"",
    'message.deathbound.soulforge.none': "\"You're not wearing a %s I can work, friend.\"",
    'soulforge.deathbound.helm': 'helm',
    'soulforge.deathbound.chestplate': 'chestplate',
    'soulforge.deathbound.leggings': 'leggings',
    'soulforge.deathbound.boots': 'boots',
    'entity.deathbound.gravedigger': 'Aldous, the Traveler',
    'entity.deathbound.prophet': 'The Chained Prophet',
    'deathbound.lost_soul.says': 'A Lost Soul whispers: "%s"',
    'message.deathbound.ferry_keep': "Everything you carried sank into the dry river. The Ferryman will be holding it.",
    'message.deathbound.reclaim.given': '"The river brought me these. They smell of you." The Ferryman hands back your belongings.',
    'message.deathbound.reclaim.none': '"The river brought me nothing of yours, bearer. Not yet."',
    'subtitles.deathbound.dread.whisper': 'Something whispers',
    'subtitles.deathbound.npc.murmur': 'Someone murmurs',
    'subtitles.deathbound.dread.footstep': 'Footsteps behind you',
    'subtitles.deathbound.dread.twitch': 'Bones rattle',
    'subtitles.deathbound.dread.toll': 'A distant bell tolls',
    'subtitles.deathbound.dread.heartbeat': 'Your heart pounds',
    'subtitles.deathbound.dread.wail': 'Something wails, far off',
    # what an NPC says when you come back to the top of a conversation, instead of the whole greeting again
    'npc.deathbound.again.ferryman': "Ask, then. The river isn't going anywhere. Not anymore.",
    'npc.deathbound.again.gravedigger': "Go on. I'm listening.",
    'npc.deathbound.again.prophet': 'What else? I have time. Time is all I have.',
    'npc.deathbound.again.king': 'What else? I have time. Time is all I have.',
    'npc.deathbound.again.collector': 'Hm? Something else?',
    'npc.deathbound.again.bonesmith': '*Clack?*',
    'npc.deathbound.again.mira': 'What else? I have to keep counting.',
    'npc.deathbound.again.lamplighter': 'Something else? The lamps can spare me a moment.',
    'npc.deathbound.again.sentry': 'Anything else? Quickly. I am on duty.',
    'npc.deathbound.again.kid': 'What? WHAT? Hurry, Tib is coming.',
    'entity.deathbound.mira': 'Mira',
    'entity.deathbound.lamplighter': 'The Lamplighter',
    'entity.deathbound.sentry': 'The Sentry',
    'entity.deathbound.skeleton_kid': 'Skeleton Child',
    'entity.deathbound.lantern_wisp': 'Lantern Wisp',
    'item.deathbound.underworld_journal': 'The Underworld Journal',
    'item.deathbound.underworld_journal.lore0': 'Every favor the dead have asked of you.',
    'item.deathbound.aldous_lantern': "Aldous's Lantern",
    'item.deathbound.aldous_lantern.lore0': 'For Mira, in the line at the Great Door.',
    'item.deathbound.miras_ribbon': "Mira's Ribbon",
    'item.deathbound.miras_ribbon.lore0': 'For her father, at the forge in Lantern\'s End.',
    'item.deathbound.ferrymans_oar': "The Ferryman's Oar",
    'item.deathbound.ferrymans_oar.lore0': 'It still knows the river.',
    'item.deathbound.pips_ball': "Pip's Ball",
    'item.deathbound.pips_ball.lore0': 'Knucklebones, sewn tight. The best ball.',
    'item.deathbound.sentrys_tag': "The Sentry's Tag",
    'item.deathbound.sentrys_tag.lore0': 'A name, scratched in tin.',
    'item.deathbound.lantern_wisp': 'Lantern Wisp',
    'item.deathbound.lantern_wisp.lore0': 'A wisp of the Lamplighter\'s lamp. Use to wake it, or let it sleep.',
    'item.deathbound.lantern_wisp.lore1': 'It lights the dark, and rings when the dead come close.',
    'message.deathbound.wisp.wake': 'The wisp wakes, and settles at your shoulder.',
    'message.deathbound.wisp.sleep': 'The wisp goes back into its lantern.',
    'quest.deathbound.journal_new': 'Journal: %s',
    'quest.deathbound.journal_done': 'Journal: %s (done)',
    'journal.deathbound.title': 'THE UNDERWORLD JOURNAL',
    'journal.deathbound.intro': 'The dead ask for things. Not much. A lantern carried, an oar found, a name remembered.',
    'journal.deathbound.count': 'In hand: %s\nDone: %s',
    'journal.deathbound.empty': 'Nobody has asked you for anything yet. Talk to the people down here. Some of them have been waiting a long time.',
    'quest.deathbound.mira.title': "Aldous's Lantern",
    'quest.deathbound.mira.giver': 'Asked by Aldous, at the forge in Lantern\'s End',
    'quest.deathbound.mira.stage1': 'Take Aldous\'s lantern to his daughter Mira. She is in the line of souls at the Great Door, near the back.',
    'quest.deathbound.mira.stage2': 'Mira gave you her ribbon. Bring it back to Aldous.',
    'quest.deathbound.mira.stage3': 'Mira is in the line, and she is not scared. Aldous knows. Done.',
    'quest.deathbound.oar.title': "The Ferryman's Oar",
    'quest.deathbound.oar.giver': 'Asked by the Ferryman, at the Landing',
    'quest.deathbound.oar.stage1': 'Find the Ferryman\'s oar, in his old boat-shed at the south edge of the Ghostwood.',
    'quest.deathbound.oar.stage2': 'The oar is back with the Ferryman. Done.',
    'quest.deathbound.lamps.title': 'Three Lights',
    'quest.deathbound.lamps.giver': 'Asked by the Lamplighter, in Lantern\'s End',
    'quest.deathbound.lamps.stage1': 'Bring the Lamplighter three Soul Jars (a jar and a soul, or bought from the Collector).',
    'quest.deathbound.lamps.stage2': 'The lamps of Lantern\'s End burn a little brighter. You have a Lantern Wisp. Done.',
    'quest.deathbound.ball.title': 'The Best Ball',
    'quest.deathbound.ball.giver': 'Asked by Pip, in the streets of Lantern\'s End',
    'quest.deathbound.ball.stage1': 'Find the children\'s ball. It rolled under the old footbridge at the Crossing, in the dry river.',
    'quest.deathbound.ball.stage2': 'The children have their ball back. Done.',
    'quest.deathbound.name.title': 'A Name in Tin',
    'quest.deathbound.name.giver': 'Asked by the Sentry, on the Western Watch',
    'quest.deathbound.name.stage1': 'Find the Sentry\'s tag, halfway across the broken bridge from the Watch to the Hollow.',
    'quest.deathbound.name.stage2': 'His name is Corwin. He remembers it now. Done.',
    'entity.deathbound.bonesmith': 'Clatter, the Bonesmith',
    'block.deathbound.soulwater': 'Soulwater',
    'item.deathbound.soulwater_bucket': 'Bucket of Soulwater',
    'temper.deathbound.rank1': 'Tempered in the Mere: I',
    'temper.deathbound.rank2': 'Tempered in the Mere: II',
    'temper.deathbound.rank3': 'Tempered in the Mere: III',
    'temper.deathbound.effect1': '+1.5 attack damage',
    'temper.deathbound.effect2': 'Hits light up the dead, and drag at them',
    'temper.deathbound.effect3': 'Every dead thing you put down gives back a little life',
    'message.deathbound.temper.done': 'Clatter plunges your %1$s into the Mere. The water hisses, and lights come up off the blade. (%4$s)',
    'message.deathbound.temper.wanting': '*He counts on his fingers: %2$s Grave Rune(s), %3$s souls. Then he holds out his empty hand.*',
    'message.deathbound.temper.none': '*He taps your hand and shakes his whole head. A weapon. Something with an edge.*',
    'message.deathbound.temper.already': '*He turns your %1$s in the light and hands it back. The Mere has already had it.*',
    'message.deathbound.temper.order': '*He holds up fewer fingers. One quench at a time.*',
    'message.deathbound.temper.gate2': '*He points toward the Great Door and mimes a huge figure kneeling. Not yet.*',
    'message.deathbound.temper.gate3': '*He mimes drawing a bow, points west, and shakes his head. Not while the Hunter lives.*',
    'npc.deathbound.again.throne': 'The throne waits.',
    # the three keys to the Death King's gate (see Puzzles.java)
    'block.deathbound.grave_lamp': 'Grave Lamp',
    'block.deathbound.fare_bowl': 'Fare Bowl',
    'block.deathbound.armor_rack': 'Armor Rack',
    'block.deathbound.weapon_rack': 'Weapon Rack',
    'block.deathbound.grave_anvil': 'Grave Anvil',
    'block.deathbound.bedroll': 'Bedroll',
    'item.deathbound.hunters_bow': "The First Hunter's Bow",
    'item.deathbound.hunters_bow.lore0': 'Bone and sinew. Its arrows glow, and whatever they hit stays lit.',
    'item.deathbound.hunters_bow.lore1': 'He never missed. Now you might not either.',
    'item.deathbound.hunters_bow.lore2': 'Hold the draw past full and it remembers the hunt.',
    'block.deathbound.grave_bell': 'Grave Bell',
    'block.deathbound.watcher_skull': 'Watcher Skull',
    'block.deathbound.lock_kings': 'Lock of the Crown',
    'block.deathbound.lock_watchers': 'Lock of the Eye',
    'block.deathbound.lock_bell': 'Lock of the Bell',
    'item.deathbound.sigil_kings': 'Sigil of the Kings',
    'item.deathbound.sigil_kings.lore0': "It rose out of the old King's empty tomb.",
    'item.deathbound.sigil_kings.lore1': "One of three keys to the Death King's gate.",
    'item.deathbound.sigil_watchers': 'Sigil of the Watchers',
    'item.deathbound.sigil_watchers.lore0': 'The skulls at the top of the Spire kept it.',
    'item.deathbound.sigil_watchers.lore1': "One of three keys to the Death King's gate.",
    'item.deathbound.sigil_bell': 'Sigil of the Bell',
    'item.deathbound.sigil_bell.lore0': "It fell out of the bell at Lantern's End.",
    'item.deathbound.sigil_bell.lore1': "One of three keys to the Death King's gate.",
    'puzzle.deathbound.clue_lamps': 'Carved over the tombs: "LIGHT THE KINGS\' LAMPS IN THE ORDER THEY RULED. THE NAMELESS TOMB STAYS DARK." (Read the names on the tombs.)',
    'puzzle.deathbound.clue_watchers': 'Cut into the floor: "TURN EVERY WATCHER TO LOOK OUT OF ITS WINDOW."',
    'puzzle.deathbound.clue_bell': 'Cut into the bell tower: "TOLL ONCE FOR EACH KING NAMED IN THE TOMB OF KINGS, AND ONCE FOR THE KING ON THE THRONE."',
    'puzzle.deathbound.clue_gate': 'The gate is sealed. Three locks hold it: a crown, an eye, and a bell.',
    'puzzle.deathbound.lamps_wrong': "Out of order. Every flame goes out at once. The dead don't like being counted wrong.",
    'puzzle.deathbound.lamps_nameless': "That tomb has no King in it yet. Every flame goes out at once.",
    'puzzle.deathbound.lamp_ok0': "Halden's lamp burns. (1 of 3)",
    'puzzle.deathbound.lamp_ok1': "Ysolde's lamp burns. (2 of 3)",
    'puzzle.deathbound.lamp_ok2': "The King's lamp burns. (3 of 3)",
    'puzzle.deathbound.hint_lamp': 'A cold grave lamp. Use it to light it.',
    'puzzle.deathbound.hint_skull': 'This skull is staring in at you. Use it to turn it.',
    'puzzle.deathbound.skull_out': 'This one looks out now. (%s of 4)',
    'puzzle.deathbound.skull_in': 'It still looks in. (%s of 4 look out)',
    'puzzle.deathbound.toll': 'The bell tolls. (%s)',
    'puzzle.deathbound.names_deep': "The Kings' names are cut too deep to break.",
    'puzzle.deathbound.after_warden': "Beyond the Door, the Death King's gate is sealed with three locks. The Prophet in the Tomb of Kings knows where the keys are hidden.",
    'puzzle.deathbound.bell_wrong': 'The last toll fades. Nothing answers.',
    'puzzle.deathbound.solved0': 'Something rises out of the empty tomb.',
    'puzzle.deathbound.solved1': 'The skulls settle. Something drops to the floor between them.',
    'puzzle.deathbound.solved2': 'The bell keeps ringing after you stop. Something falls out of it.',
    'puzzle.deathbound.lock_wants': 'This lock wants the %s.',
    'puzzle.deathbound.lock_filled': 'The lock takes the sigil. %s of 3.',
    'puzzle.deathbound.gate_open': 'THE GATE IS OPEN',
    'puzzle.deathbound.gate_open.sub': 'The Death King is waiting for you.',
}
# ---------------------------------------------------------------------------------------------------- the forgotten things
ARTIFACTS = {   # name, what it is, what the Collector says about it when he takes it
    'ferry_coin': ("Ferry Coin", "A coin from up top, worn smooth by a thumb.",
                   "A fare from the living world. The Ferryman never gives these up. Unless it hurts to look at."),
    'wooden_horse': ("Wooden Horse", "A child's toy. One ear longer than the other.",
                     "Carved by a man who wasn't a carver. Look at the ears. He loved her, whoever she was."),
    'kings_quill': ("The King's Quill", "Every law of the throne was written with this.",
                    "Funny. Read the laws back and every one of them favours the King."),
    'warden_seal': ("The Warden's Seal", "He stamped every soul he ever judged.",
                    "Nine million stamps. Not once on himself. He never thought he deserved judging."),
    'melted_chains': ("Melted Chains", "Ten chains, fused into one cold lump.",
                      "Someone was very afraid of being replaced. Ten times afraid."),
    'kings_ring': ("The King's Ring", "Taken from the old King's hand.",
                   "Pulled off his finger while he screamed. He'd worn it eight hundred years. He'll want it back."),
    'hunters_arrowhead': ("Hunter's Arrowhead", "Bone, not iron. Still sharp.",
                          "He makes them from what he hunts. He was here before the river was, you know."),
    'last_drop': ("The Last Drop", "All that's left of the Lake of Souls.",
                  "Hold it to your ear. Somebody in there is still singing."),
}
for k, (name, what, said) in ARTIFACTS.items():
    LANG[f'item.deathbound.{k}'] = name
    LANG[f'item.deathbound.{k}.lore0'] = what
    LANG[f'item.deathbound.{k}.lore1'] = 'The Collector would want this.'
    LANG[f'collector.deathbound.{k}'] = said
LANG.update({
    'entity.deathbound.collector': 'The Collector',
    'collector.deathbound.nothing': "Nothing? Then why are you standing in my vault? Go and find me something forgotten.",
    'collector.deathbound.done': "You've brought me everything. Go home. Before you become a thing someone collects.",
    'collector.deathbound.complete': "All eight. Then here is the one thing nobody else will tell you: the first one down never "
                                     "wanted the throne. Every King since has. Ask yourself which of them got to go home.",
})

LANG.update({f'deathbound.lost_soul.line.{i}': t for i, t in enumerate(LOST_SOUL_LINES)})
LANG.update({f'deathbound.say.{k}': v for k, v in SAY.items()})
for place, lines in LINGERING.items():
    LANG.update({f'deathbound.lingering.{place}.{i}': t for i, t in enumerate(lines)})
for area, (name, line) in AREAS.items():
    LANG[f'area.deathbound.{area}'] = name
    LANG[f'area.deathbound.{area}.line'] = line
for item, lines in ITEM_LORE.items():
    LANG.update({f'item.deathbound.{item}.lore{i}': t for i, t in enumerate(lines)})


# ---------------------------------------------------------------------------------------------------- sounds
def _snd(names, pitch=1.0, volume=1.0, **kw):
    return [{'name': f'minecraft:{n}', 'pitch': pitch, 'volume': volume, **kw} for n in names]


SOUNDS = {
    'dread.whisper': {'subtitle': 'subtitles.deathbound.dread.whisper', 'sounds':
        _snd([f'ambient/nether/soulsand_valley/whisper{i}' for i in range(1, 9)], 0.85, 0.9)
        + _snd([f'ambient/nether/soulsand_valley/voices{i}' for i in range(1, 6)], 0.7, 0.6)},
    'npc.murmur': {'subtitle': 'subtitles.deathbound.npc.murmur', 'sounds':
        _snd([f'ambient/nether/soulsand_valley/voices{i}' for i in range(1, 6)], 0.75, 0.8)},
    'dread.footstep': {'subtitle': 'subtitles.deathbound.dread.footstep', 'sounds':
        _snd([f'mob/skeleton/step{i}' for i in range(1, 5)], 0.8, 0.7) + _snd([f'block/bone_block/step{i}' for i in range(1, 6)], 0.7, 0.8)},
    'dread.twitch': {'subtitle': 'subtitles.deathbound.dread.twitch', 'sounds':
        _snd([f'block/bone_block/break{i}' for i in range(1, 6)], 1.3, 0.5) + _snd([f'block/bone_block/step{i}' for i in range(1, 6)], 0.6, 0.9)},
    'dread.toll': {'subtitle': 'subtitles.deathbound.dread.toll', 'sounds':
        _snd(['block/bell/bell_use01', 'block/bell/bell_use02'], 0.5, 1.0, attenuation_distance=200)},
    # far-off things for the wind to carry: wails, the creak of a gibbet chain, a voice saying something you can't make out
    'dread.wail': {'subtitle': 'subtitles.deathbound.dread.wail', 'sounds':
        _snd([f'mob/ghast/moan{i}' for i in range(1, 8)], 0.45, 0.5, attenuation_distance=64)
        + _snd(['mob/guardian/curse'], 0.5, 0.25, attenuation_distance=64)},
    'dread.air': {'sounds':
        _snd([f'ambient/nether/soulsand_valley/whisper{i}' for i in range(1, 9)], 0.7, 0.45)
        + _snd([f'ambient/nether/soulsand_valley/wind{i}' for i in range(1, 5)], 0.6, 0.6)
        + _snd([f'block/chain/step{i}' for i in range(1, 5)], 0.5, 0.35)
        + _snd([f'mob/vex/idle{i}' for i in range(1, 5)], 0.45, 0.2)
        + _snd([f'mob/ghast/moan{i}' for i in range(1, 8)], 0.4, 0.12)},
    'dread.heartbeat': {'subtitle': 'subtitles.deathbound.dread.heartbeat', 'sounds':
        _snd([f'mob/warden/heartbeat_{i}' for i in range(1, 5)], 0.8, 1.0)},
    # boss fights: Dead Voxel for the Warden at the Door, Creator for Death's first faces, Precipice once it becomes the Beast
    'music.guard': {'sounds': _snd(['music/game/nether/dead_voxel'], 1.0, 1.0, stream=True)},
    'music.death': {'sounds': _snd(['records/creator'], 1.0, 1.0, stream=True)},
    'music.reaper': {'sounds': _snd(['music/game/end/boss'], 1.0, 1.0, stream=True)},
    'music.beast': {'sounds': _snd(['records/precipice'], 1.0, 1.0, stream=True)},
    # the Hollow: something patient, with footsteps in it
    'music.hunter': {'sounds': _snd(['records/5'], 1.0, 1.0, stream=True)},
    # the empty throne, after: C418's Alpha, the quietest thing in the game; and nothing at all, for the moment he dies
    'music.throne': {'sounds': _snd(['music/game/end/alpha'], 1.0, 1.0, stream=True)},
    'music.silence': {'sounds': []},
}


def generate(write, A, D):
    for name, d in DIALOGS.items():
        if d.get('type') == 'minecraft:multi_action' and not d['actions']:   # a page must have a button: its exit becomes it
            d = {**d, 'actions': [d['exit_action']]}
            del d['exit_action']
        write(os.path.join(D, 'dialog', name + '.json'), d)
    write(os.path.join(D, 'loot_table/journal.json'), {'type': 'minecraft:chest', 'pools': [
        {'rolls': 1, 'entries': [_book(*j) for j in JOURNALS.values()]}], 'random_sequence': f'{NS}:journal'})
    write(os.path.join(A, 'sounds.json'), SOUNDS)
