"""Item icons for the dead's favors: the Journal, Aldous's lantern, Mira's ribbon, the Ferryman's oar, Pip's ball,
the Sentry's tag, and the Lantern Wisp's lantern. Plain 16x16 pixel art, drawn from row strings."""
import numpy as np


def _icon(rows, legend):
    img = np.zeros((16, 16, 4), dtype=np.uint8)
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c in legend:
                img[y, x] = legend[c]
    return img


def _h(s, a=255):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


BLANK = "................"


def journal():
    L = {'k': _h('#120c18'), 'c': _h('#3a2150'), 'C': _h('#4f2e6b'), 'p': _h('#d8cfc0'), 'P': _h('#b9ae9c'), 's': _h('#c9bfae'), 'e': _h('#a061ff')}
    return _icon([BLANK,
                  "...kkkkkkkkkk...",
                  "..kCCCCCCCCCCk..",
                  "..kCcccccccCCkp.",
                  "..kCc..ss..cCkp.",
                  "..kCc.ssss.cCkp.",
                  "..kCc.sees.cCkp.",
                  "..kCc.ssss.cCkp.",
                  "..kCc..ss..cCkp.",
                  "..kCc.s..s.cCkP.",
                  "..kCcccccccCCkP.",
                  "..kCCCCCCCCCCkP.",
                  "..kkkkkkkkkkkkP.",
                  "...PPPPPPPPPPPP.",
                  BLANK, BLANK], L)


def aldous_lantern():
    L = {'k': _h('#1a1712'), 'b': _h('#7a5a1e'), 'B': _h('#c3a14a'), 'f': _h('#ffd98f'), 'F': _h('#ffae45'), 'g': _h('#3a352c')}
    return _icon([BLANK,
                  "......kBBk......",
                  ".....k....k.....",
                  "......kBBk......",
                  ".....kbBBbk.....",
                  "....kb....bk....",
                  "....kbgffgbk....",
                  "....kbgFfgbk....",
                  "....kbgFFgbk....",
                  "....kbggggbk....",
                  "....kbBBBBbk....",
                  ".....kkkkkk.....",
                  BLANK, BLANK, BLANK, BLANK], L)


def miras_ribbon():
    L = {'r': _h('#8a6aa8'), 'R': _h('#b294cf'), 'd': _h('#5c4475')}
    return _icon([BLANK, BLANK,
                  "...rr......rr...",
                  "..rRRr....rRRr..",
                  "..rRRRr..rRRRr..",
                  "...rRRRrrRRRr...",
                  "....rrRddRrr....",
                  "......dddd......",
                  "....rrRddRrr....",
                  "...rRr....rRr...",
                  "..rRr......rRr..",
                  "..rr........rr..",
                  ".rr..........rr.",
                  BLANK, BLANK, BLANK], L)


def ferrymans_oar():
    L = {'w': _h('#46311f'), 'W': _h('#6f5134'), 'd': _h('#22170f'), 'i': _h('#5d5965')}
    return _icon(["............dWd.",
                  "...........dWWd.",
                  "..........dWWWd.",
                  ".........dWWWd..",
                  "........dWWWd...",
                  ".......iWWd.....",
                  "......dwi.......",
                  ".....dwd........",
                  "....dwd.........",
                  "...dwd..........",
                  "..dwd...........",
                  ".dWd............",
                  "dWd.............",
                  "dd..............",
                  BLANK, BLANK], L)


def pips_ball():
    L = {'b': _h('#a69a86'), 'B': _h('#e3dac4'), 'd': _h('#5b5258'), 'k': _h('#1c1820')}
    return _icon([BLANK, BLANK, BLANK,
                  ".....kkkkk......",
                  "....kBBbBBk.....",
                  "...kBBdBBbbk....",
                  "...kBdBBdBbk....",
                  "...kbBBBBBdk....",
                  "...kBBdBBbbk....",
                  "...kbBBBdBbk....",
                  "....kbbBbbk.....",
                  ".....kkkkk......",
                  BLANK, BLANK, BLANK, BLANK], L)


def sentrys_tag():
    L = {'c': _h('#3a3740'), 's': _h('#8d8d9a'), 'S': _h('#b2b2bd'), 'k': _h('#2f2f38')}
    return _icon([BLANK,
                  "....c.c.c.......",
                  "...c.....c......",
                  "..c.......c.....",
                  "..c........c....",
                  "...c....kkkkk...",
                  "....c..kSSSSSk..",
                  ".......kSkskSk..",
                  ".......kSSSSSk..",
                  ".......kSksSSk..",
                  ".......kSSSSSk..",
                  "........kkkkk...",
                  BLANK, BLANK, BLANK, BLANK], L)


def lantern_wisp():
    L = {'k': _h('#151419'), 'i': _h('#4a4651'), 'f': _h('#c597ff'), 'F': _h('#fbf6ff'), 'm': _h('#7a2fe6'), 'e': _h('#ffffff')}
    return _icon([BLANK,
                  ".......ii.......",
                  "......i..i......",
                  ".....kiiiik.....",
                  "....kk....kk....",
                  "....k.mffm.k....",
                  "....k.fFFf.k....",
                  "....k.e..e.k....",
                  "....k.fFFf.k....",
                  "....k.mffm.k....",
                  "....kk....kk....",
                  ".....kiiiik.....",
                  BLANK, BLANK, BLANK, BLANK], L)
