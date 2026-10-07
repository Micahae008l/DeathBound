"""How each NPC's conversation is put together (see npc/Talk.java, which reads the generated TalkData.java).

A conversation is no longer one fixed page. Talking to someone builds it on the spot:
  * the greeting: the first one whose condition holds and that you haven't heard yet, in full; once you've heard
    everything that applies, a short "back again" line instead;
  * topics: questions whose condition holds, behind an "Ask about..." menu when there are more than two; ones you
    haven't asked yet are marked;
  * services (trade, forge, temper) and the favor button, each with its own condition.
Conditions: '' always; warden / slain / hunter / ended / end1-3 (story); !x negates; q:ID=N, q:ID>=N, q:ID<N (favors).
Several conditions separated by commas must all hold.

Importing this module also adds the new lore pages to lore.DIALOGS and their lines to lore.LANG.
"""
import json
import os
import lore
from lore import _dialog

D = lore.DIALOGS
L = lore.LANG

# ---------------------------------------------------------------------------------------------------- new pages
NEW = {
    # ------------------------------------------------------------------ the Ferryman
    'ferryman.oar': _dialog('ferryman', [
        "*The oar leans against his post, where he can reach it without looking.*",
        "I took her out on the mud last night. Pushed off from nothing, rowed nowhere. Best night I've had in a hundred years.",
        "*If the river ever comes back, bearer, you ride free.*",
    ], []),
    'ferryman_door': _dialog('ferryman', [
        "What's behind the Door? The Lake of Souls. Or the hole where it used to be.",
        "Every soul I rowed down was meant to cross that Lake and rest on the far shore. Then he drank it.",
        "*You opened the Door. Don't expect thanks. They're waiting for the water, not the way.*",
    ], []),
    'ferryman_silas': _dialog('ferryman', [
        "Silas. I rowed him down myself. A boy with a chain and a sword too big for him, asking how far to the throne.",
        "He laughed the whole way. Said he was going to beat the Underworld, like it was a game.",
        "*He didn't laugh much after he won. The ones who win never do.*",
    ], []),
    'ferryman_hunter': _dialog('ferryman', [
        "The Hunter's dead? Then the west is quiet for the first time since before there was a King.",
        "I rowed him down too. The very first. He tipped me a coin, then asked which way the animals went.",
        "*Whatever he was, he was honest. I'll miss that.*",
    ], []),
    # ------------------------------------------------------------------ Aldous
    'gravedigger.mira': _dialog('gravedigger', [
        "*He's tied her ribbon round his wrist.* She used to do that. Every morning, so I wouldn't forget her at the forge.",
        "She told me to go home. She was always the sensible one.",
        "*I'll stay a little longer. Somebody should keep the forge lit, in case she ever comes this way.*",
    ], []),
    # once he has her ribbon, his later greetings stop asking whether you've seen her
    'gravedigger.warden_found': _dialog('gravedigger', [
        "You beat the Warden? Then one day the Door opens, and she walks through it. Good. That's where she was going.",
        "*He turns the ribbon on his wrist.* The blind man came to me in a dream. He said a King can send a soul home. I told him she already knows the way.",
        "If you're after the bell at Lantern's End: I put a ladder up the tower. Nobody's rung it in years. Nobody living.",
    ], []),
    'gravedigger.slain_found': _dialog('gravedigger', [
        "It's over? Then the throne is empty. The blind man says whoever sits there can do anything.",
        "She told me to go home. I keep thinking whoever sits there could make that true for everyone. ...Or for nobody.",
        "*Don't let it be me. I've already had my miracle.*",
    ], []),
    'gravedigger_home': _dialog('gravedigger', [
        "Home? Up there it's spring, probably. The bread's gone hard and the dog's gone to my sister's.",
        "*I'll go. When the forge has had enough of me. Not yet.*",
    ], []),
    'gravedigger_after': _dialog('gravedigger', [
        "The Death King's dead. The line will move again, they say. She'll get across.",
        "*Then I can stop listening for her. That's the hardest part, you know. Stopping.*",
    ], []),
    # ------------------------------------------------------------------ the Prophet
    'prophet_kings': _dialog('prophet', [
        "Halden came first, of the ones you'd call Kings. A miner. He dug down with a pickaxe and took the throne with it.",
        "Ysolde shot him off it from the far end of the hall. She was the best of us. She built things: the bridges you walk on, the little golem at the Mere.",
        "*And then me. I strangled Ysolde with her own bowstring. That's how it works, bearer. That's always how it works.*",
    ], []),
    'prophet_hunter': _dialog('prophet', [
        "The Hunter was here before the throne had a name. The first living soul to come down.",
        "They offered him the crown. He laughed and walked west, and he's hunted whatever comes down ever since.",
        "*Don't go west unless you mean it. He never misses twice.*",
    ], []),
    'prophet_tomb': _dialog('king', [
        "Do you know where he hung me? Over my own grave. The empty one, in the Tomb of Kings.",
        "Every King gets a tomb. Mine had no body in it, because the dead can't die. So he made me hang over it, every day, for every year.",
        "*He thought it was cruel. It was. It also taught me patience.*",
    ], []),
    # ------------------------------------------------------------------ the Collector
    'collector_hunter': _dialog('collector', [
        "You killed him? The first one?",
        "*He's quiet for a long time. Then he holds out his hand.* The arrowhead. You kept it. Of course you did. That one goes on the top shelf.",
    ], []),
    'collector_silas': _dialog('collector', [
        "What did he leave? A throne nobody wants and a hole where a lake used to be.",
        "And his ring. Kings always leave a ring.",
        "*Every King thinks he's the last one. Every King is wrong. I have their rings to prove it.*",
    ], []),
    # ------------------------------------------------------------------ Clatter
    'bonesmith.slain': _dialog('bonesmith', [
        "*He points toward the Citadel and mimes a crown falling off a head. He nods, slowly.*",
        "*Then he holds out his hand. Weapons first. Celebrating later.*",
    ], []),
    'bonesmith_blades': _dialog('bonesmith', [
        "*He mimes a row of soldiers, a bow, a crown. Then each soldier falling over.*",
        "Ysolde's guard. He made every one of their blades. None of them came back for them.",
        "*He points at the coffin by his forge. It's full of swords.*",
    ], []),
    'bonesmith_ranks': _dialog('bonesmith', [
        "*One finger: he dips a blade, and it comes out keener.*",
        "*Two fingers: he mimes a ghost lighting up and stumbling when it's struck. He won't do this one until the Warden has knelt to you.*",
        "*Three fingers: he strikes something down, then breathes in, deep, as if its life went into him. Not while the Hunter lives.*",
    ], []),
    # ------------------------------------------------------------------ Mira
    'mira.met': _dialog('mira', [
        "Is Papa all right? Did he go home?",
        "*She holds the lantern up close to her face, as if it's warm.* I'm not scared anymore. I wasn't really before.",
    ], []),
    'mira_line': _dialog('mira', [
        "It's very long and very slow. Everybody's nice, mostly. A man in front of me keeps telling the same joke.",
        "*Sometimes the line moves and everyone cheers, even though it's only one step.*",
    ], []),
    'mira_friend': _dialog('mira', [
        "My friend? She doesn't have a face. She lost it, she says, but she doesn't mind.",
        "*She helps me count. She's very good at big numbers.*",
    ], []),
    # ------------------------------------------------------------------ the Lamplighter
    'lamplighter.lit': _dialog('lamplighter', [
        "Look at the street. Three lamps burning properly, for the first time in years.",
        "*Your wisp's been happy? It hums when you're near. I can hear it.*",
    ], []),
    'lamplighter_lamps': _dialog('lamplighter', [
        "Why light them? Because somebody might be walking home in the dark.",
        "*Nobody walks home down here. That's not the point. The point is the light's there if they do.*",
    ], []),
    'lamplighter_town': _dialog('lamplighter', [
        "Lantern's End was where the dead stopped on the way to the Door. A last night somewhere warm. Bread, a bed, a song.",
        "*Then the line stopped moving, and the town stopped being a stop. Now it's just where they wait.*",
    ], []),
    # ------------------------------------------------------------------ the Sentry
    'sentry.named': _dialog('sentry', [
        "Corwin. I've been saying it all day. Corwin. It sounds right.",
        "*I remember my mother calling it down the street. Funny, what comes back with a name.*",
    ], []),
    'sentry.hunter': _dialog('sentry', [
        "They say the Hunter is dead. Is it true? You did it?",
        "*He looks west for a long moment. Then, for the first time, he turns his back on it.* Then my watch is over.",
    ], []),
    'sentry_post': _dialog('sentry', [
        "Orders are orders. Watch the west, warn the King.",
        "*The King's gone. The orders aren't. Someone has to be faithful to something.*",
    ], []),
    'sentry_corwin': _dialog('sentry', [
        "I had a sergeant who snored, a girl by the mill, and a dog called Biscuit.",
        "*Biscuit. Of all the things to come back.*",
    ], []),
    # ------------------------------------------------------------------ the children
    'kid.ball': _dialog('kid', [
        "We've been playing ALL DAY. Tib kicked it in the fountain twice.",
        "*You're our favourite alive person. Don't tell the Lamplighter.*",
    ], []),
    'kid_game': _dialog('kid', [
        "One of you is the Warden and everyone else is the line. The Warden points and says 'judged!' and you have to go in the fountain.",
        "*If you don't get judged you go through the Door. The Door is the big rock. Nobody's got through yet.*",
    ], []),
    'kid_dead': _dialog('kid', [
        "Being alive? I don't remember much. I remember being cold. I'm not cold now.",
        "*Being dead is fine. There's a lot of time to play.*",
    ], []),
}
D.update(NEW)

# Clatter's and Aldous's menus: short labels, the cost and what it does on hover
D['bonesmith_temper'] = _dialog('bonesmith', [
    "*He holds out a hand for the weapon you're holding, then points at the black water.*",
], [("Rank I", 'custom:temper/1', "1 rune, 12 souls. +1.5 attack damage."),
    ("Rank II", 'custom:temper/2', "2 runes, 20 souls. +3 attack damage; your hits light up the dead and drag at them."),
    ("Rank III", 'custom:temper/3', "3 runes, 32 souls. +4.5 attack damage; every dead thing you put down gives back a little life.")],
    leave='Back')
D['gravedigger_forge'] = _dialog('gravedigger', [
    "Wear the piece you want forged. Runes and souls, and I'll beat the strength of the dead into it.",
], [("Helm", 'custom:soulforge/head', "1 rune, 10 souls. +1 armor, +1 toughness, Underworld hits 4% softer."),
    ("Chestplate", 'custom:soulforge/chest', "2 runes, 22 souls. +2 armor, +2 toughness, Underworld hits 8% softer."),
    ("Leggings", 'custom:soulforge/legs', "2 runes, 16 souls. +2 armor, +1 toughness, Underworld hits 6% softer."),
    ("Boots", 'custom:soulforge/feet', "1 rune, 10 souls. +1 armor, +1 toughness, Underworld hits 4% softer.")],
    leave='Back')
# the throne doesn't tell you what each choice does: you find out
t = D['throne']
for a in t['actions']:
    a.pop('tooltip', None)

# ---------------------------------------------------------------------------------------------------- who says what
TALK = {
    'ferryman': dict(
        greet=[('end1', 'ferryman.end1'), ('end2', 'ferryman.end2'), ('end3', 'ferryman.end3'), ('slain', 'ferryman.slain'),
               ('warden', 'ferryman.warden'), ('q:oar=2', 'ferryman.oar'), ('', 'ferryman')],
        topics=[('ferryman_who', "Who are you?", ''), ('ferryman_where', "What is this place?", ''),
                ('ferryman_river', "What happened to the river?", ''), ('ferryman_others', "Who else came down alive?", ''),
                ('ferryman_win', "What happens if I win?", 'warden,!slain'), ('ferryman_door', "What's behind the Door?", 'warden'),
                ('ferryman_silas', "Who was Silas?", 'slain'), ('ferryman_hunter', "The Hunter is dead.", 'hunter')],
        services=[("Do you have my things?", 'custom:reclaim', ''), ("Show me what you sell.", 'custom:trade', ''),
                  ("Can I help you?", 'custom:quest/ferryman', 'q:oar=0'), ("About your oar...", 'custom:quest/ferryman', 'q:oar=1')],
        back=["Back again. The river's no wetter.", "*He leans on his pole and waits for you to speak.*", "Ask, then. I'm not going anywhere."],
        ask="*What do you want to know?*"),
    'gravedigger': dict(
        greet=[('end1', 'gravedigger.end1'), ('end2', 'gravedigger.end2'), ('end3', 'gravedigger.end3'),
               ('slain,q:mira>=3', 'gravedigger.slain_found'), ('slain', 'gravedigger.slain'), ('q:mira>=3', 'gravedigger.mira'),
               ('warden,q:mira>=3', 'gravedigger.warden_found'), ('warden', 'gravedigger.warden'), ('', 'gravedigger')],
        topics=[('gravedigger_why', "Why are you down here?", 'q:mira<3'), ('gravedigger_chain', "Where did you get your chain?", ''),
                ('gravedigger_village', "What happened to this village?", ''), ('gravedigger_home', "Will you go home now?", 'q:mira>=3'),
                ('gravedigger_after', "What will you do now?", 'slain')],
        services=[("Forge my armor.", 'dialog:gravedigger_forge', ''), ("Let's trade.", 'custom:trade', ''),
                  ("Can I help you find her?", 'custom:quest/gravedigger', 'q:mira=0'),
                  ("About Mira...", 'custom:quest/gravedigger', 'q:mira=1'),
                  ("Mira gave me something.", 'custom:quest/gravedigger', 'q:mira=2')],
        back=["Back again, friend? The forge is hot.", "*He wipes his hands on his apron.* What can I do for you?", "Mind the sparks."],
        ask="*What is it?*"),
    'prophet': dict(
        greet=[('end1', 'prophet.end1'), ('end2', 'prophet.end2'), ('end3', 'prophet.end3'), ('slain', 'prophet.slain'),
               ('warden', 'prophet.warden'), ('', 'prophet')],
        topics=[('prophet_saw', "What did you see?", '!slain'), ('prophet_guard', "How do I get past the Warden?", '!warden'),
                ('prophet_seals', "His gate is sealed.", 'warden,!slain'), ('prophet_found', "How did the chain find me?", 'warden,!slain'),
                ('prophet_death', "How do I beat the Death King?", 'warden,!slain'), ('prophet_mira', "Is there a girl named Mira here?", '!slain,q:mira<2'),
                ('prophet_kings', "Tell me about the Kings.", ''), ('prophet_hunter', "Who is the Hunter?", '!hunter'),
                ('king_chain', "You sent my chain.", 'slain'), ('king_trust', "Why should I trust you?", 'slain,!ended'),
                ('prophet_tomb', "Why did he hang you here?", 'slain'), ('king_changed', "You've changed.", 'end2')],
        services=[],
        back=["You're back. I can hear your chain.", "*He turns his blind face toward you.* Ask.", "Still alive? Good."],
        ask="*Ask.*"),
    'collector': dict(
        greet=[('', 'collector')],
        topics=[('collector_who', "Who are you?", ''), ('collector_things', "What do you collect?", ''),
                ('collector_kings', "What do you know about the Kings?", ''), ('collector_west', "Is there anything out west?", '!hunter'),
                ('collector_hunter', "I killed the Hunter.", 'hunter'), ('collector_silas', "What did the Death King leave behind?", 'slain')],
        services=[("I found something for you.", 'custom:collect', ''), ("Show me what you sell.", 'custom:trade', '')],
        back=["Mind the floor. Again.", "*He peers at your pockets.* Well?", "Back so soon? What have you found?"],
        ask="*Curious, are we?*"),
    'bonesmith': dict(
        greet=[('slain', 'bonesmith.slain'), ('', 'bonesmith')],
        topics=[('bonesmith_mere', "What is this place?", ''), ('bonesmith_who', "Who made you?", ''),
                ('bonesmith_ranks', "What does tempering do?", ''), ('bonesmith_blades', "Who did you make blades for?", 'warden')],
        services=[("Temper my weapon.", 'dialog:bonesmith_temper', '')],
        back=["*Clack.*", "*He taps the water, then your weapon.*", "*Clack-clack?*"],
        ask="*He tilts his whole head. Go on.*"),
    'mira': dict(
        greet=[('q:mira>=2', 'mira.met'), ('', 'mira')],
        topics=[('mira_who', "Who are you?", ''), ('mira_friend', "Who's your friend?", ''), ('mira_line', "What's the line like?", 'q:mira>=2')],
        services=[("Your father sent me.", 'custom:quest/mira', 'q:mira=1')],
        back=["You're back! I was at seven hundred and two.", "*She waves with both hands.*", "Hello again!"],
        ask="*What?*"),
    'lamplighter': dict(
        greet=[('q:lamps=2', 'lamplighter.lit'), ('', 'lamplighter')],
        topics=[('lamplighter_who', "Who are you?", ''), ('lamplighter_town', "What was this town like?", ''),
                ('lamplighter_lamps', "Why light them at all?", 'q:lamps>=2')],
        services=[("Can I help with anything?", 'custom:quest/lamplighter', 'q:lamps=0'),
                  ("About the lamps...", 'custom:quest/lamplighter', 'q:lamps=1')],
        back=["Evening. Still evening.", "*He lifts his lamp to see you better.*", "The lamps are holding. Barely."],
        ask="*Yes?*"),
    'sentry': dict(
        greet=[('hunter', 'sentry.hunter'), ('q:name=2', 'sentry.named'), ('', 'sentry')],
        topics=[('sentry_guard', "What are you guarding?", '!hunter'), ('sentry_post', "Why do you stay?", ''),
                ('sentry_corwin', "What do you remember now?", 'q:name=2')],
        services=[("Is something wrong?", 'custom:quest/sentry', 'q:name=0'), ("About your tag...", 'custom:quest/sentry', 'q:name=1')],
        back=["Halt. ...Oh. You again.", "*He doesn't take his eyes off the west.*", "Still nothing moving out there."],
        ask="*Quickly. I'm on watch.*"),
    'kid': dict(
        greet=[('q:ball=2', 'kid.ball'), ('', 'kid')],
        topics=[('kid_who', "Who are you?", ''), ('kid_game', "How do you play Judgement?", ''), ('kid_dead', "Do you miss being alive?", 'q:ball=2')],
        services=[("Can I help with anything?", 'custom:quest/kid', 'q:ball=0'), ("About your ball...", 'custom:quest/kid', 'q:ball=1')],
        back=["You came back! Do you want to play?", "*Pip waves a bony arm.* Hi! Hi!", "Shh, we're hiding from Tib."],
        ask="*What? WHAT?*"),
}

# every topic page goes back to its NPC's question list; every favor page and service menu back to the NPC
QUEST_OWNER = {'mira': 'gravedigger', 'oar': 'ferryman', 'lamps': 'lamplighter', 'ball': 'kid', 'name': 'sentry'}
BACKS = ('Back', 'Not now.', "I'll give it to him.")


def exit_to(d, kind, npc, label='Back'):
    d['exit_action'] = {'label': {'text': label}, 'width': 250, 'action': {'type': 'minecraft:custom', 'id': f'{lore.NS}:{kind}', 'payload': npc}}


def fold_back(d, kind, npc, default='Back'):
    """The page's own Back-style button becomes its exit: one button fewer, and it goes where it should."""
    label = next((a['label']['text'] for a in d['actions'] if a['label']['text'] in BACKS), default)
    d['actions'] = [a for a in d['actions'] if a['label']['text'] not in BACKS]
    exit_to(d, kind, npc, label)


for npc, spec in TALK.items():
    for key, _, _ in spec['topics']:
        fold_back(D[key], 'back', npc)
for key in ('gravedigger_forge', 'bonesmith_temper'):
    exit_to(D[key], 'talk', key.split('_')[0])
for key, d in D.items():
    if key.startswith('quest.'):
        npc = 'mira' if key in ('quest.mira.alone', 'quest.mira.met', 'quest.mira.met_after') else QUEST_OWNER[key.split('.')[1]]
        fold_back(d, 'talk', npc)

for npc, spec in TALK.items():
    for i, line in enumerate(spec['back']):
        L[f'talk.deathbound.{npc}.back.{i}'] = line
    L[f'talk.deathbound.{npc}.ask'] = spec['ask']
L.update({'talk.deathbound.ask': 'Ask about...', 'talk.deathbound.leave': 'Leave', 'talk.deathbound.back': 'Back', 'rewards.deathbound.from': 'From %s'})


def write_java(root):
    q = json.dumps
    rows = []
    for npc, spec in TALK.items():
        greet = ', '.join(f'new String[] {{{q(w)}, {q(k)}}}' for w, k in spec['greet'])
        topics = ', '.join(f'new Talk.Topic({q(k)}, {q(lab)}, {q(w)})' for k, lab, w in spec['topics'])
        services = ', '.join(f'new Talk.Service({q(lab)}, {q(tgt)}, {q(w)})' for lab, tgt, w in spec['services'])
        backs = ', '.join(q(b) for b in spec['back'])
        rows.append(f'\t\tMap.entry({q(npc)}, new Talk.Npc({q(npc)}, List.<String[]>of({greet}),\n\t\t\tList.of({topics}),\n\t\t\tList.of({services}),\n\t\t\tList.of({backs}), {q(spec["ask"])}))')
    body = ',\n'.join(rows)
    path = os.path.join(root, 'src/main/java/com/deathbound/npc/TalkData.java')
    with open(path, 'w', newline='\n', encoding='utf-8') as f:
        f.write(f"""package com.deathbound.npc;

import java.util.List;
import java.util.Map;

/** GENERATED by tools/datagen.py from tools/talk.py. Do not edit by hand. */
final class TalkData {{
	static final Map<String, Talk.Npc> NPCS = Map.ofEntries(
{body});

	private TalkData() {{}}
}}
""")
