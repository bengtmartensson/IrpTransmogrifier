#! /bin/sh

IRPTRANSMOGRIFIER=../../../tools/irptransmogrifier

function generate {
    $IRPTRANSMOGRIFIER --tsv $2 $3 $4 $5 decode --namedinput "$1" > "$1".exp
}

function decodeNamed {
    $IRPTRANSMOGRIFIER --tsv $2 $3 $4 $5 decode --namedinput "$1" | diff - "$1".exp
}

#TRANSMOGRIFY=generate
TRANSMOGRIFY=decodeNamed

# No decodes. This file is serial date
$TRANSMOGRIFY Airboard.ict
$TRANSMOGRIFY Aiwa2_Aiwa.ict
$TRANSMOGRIFY Aiwa_left.ict
$TRANSMOGRIFY Amino_0_0.ict --frequencytolerance -1
$TRANSMOGRIFY Amino_0_0_36K.ict
$TRANSMOGRIFY Anthem.ict
$TRANSMOGRIFY Apple.ict
$TRANSMOGRIFY Apple2.ict
$TRANSMOGRIFY Apple2_Pause.ict
$TRANSMOGRIFY Apple_Down.ict
$TRANSMOGRIFY Audiovox_V0642.ict
$TRANSMOGRIFY BO_0620.ict
$TRANSMOGRIFY BOarrowUp.ict
$TRANSMOGRIFY BOnum5.ict
$TRANSMOGRIFY BOvolUp.ict
$TRANSMOGRIFY BandO.ict
$TRANSMOGRIFY Base4_26Bit.ict
$TRANSMOGRIFY Blaupunkt.ict
$TRANSMOGRIFY Blaupunkt_Chplus.ict
$TRANSMOGRIFY Bose.ict
$TRANSMOGRIFY CanalSat-38.ict --frequencytolerance -1
$TRANSMOGRIFY CanalSat-38_3.ict --frequencytolerance -1
$TRANSMOGRIFY CanalSat.ict
$TRANSMOGRIFY CanalSatLD.ict --frequencytolerance -1
$TRANSMOGRIFY DIshNetwork_0775.ict
$TRANSMOGRIFY "Denon-K Denon.ict"
$TRANSMOGRIFY Denon.ict
$TRANSMOGRIFY Denon_left.ict
$TRANSMOGRIFY Denon_short.ict --frequencytolerance 4000
$TRANSMOGRIFY Dgtec.ict
$TRANSMOGRIFY Dgtec2.ict
$TRANSMOGRIFY "Digivision DVT-3025EU.ict"
$TRANSMOGRIFY "Digivison DVT-3025EU Two.ict"
$TRANSMOGRIFY DirecTV_Pronto.txt
$TRANSMOGRIFY DishPlayer.ict
$TRANSMOGRIFY Dish_Network.ict
$TRANSMOGRIFY Dysan_Pronto.txt
$TRANSMOGRIFY Elan.ict
$TRANSMOGRIFY Emerson_0282.ict
$TRANSMOGRIFY EpsonPowerLiteEMP835.ict
$TRANSMOGRIFY EpsonPowerLiteEMP835_svideo.ict
$TRANSMOGRIFY EpsonPowerLite_Pronto.txt

# Unknown, No decodes
$TRANSMOGRIFY Epson_8100.txt
$TRANSMOGRIFY Epson_UnkProj.txt

# ???. No decodes
$TRANSMOGRIFY F12-shortLO.ict
$TRANSMOGRIFY F12.ict
$TRANSMOGRIFY Fujitsu_pronto.txt
$TRANSMOGRIFY "GI Cable.ict"
$TRANSMOGRIFY GI-4DTV.ict
$TRANSMOGRIFY GI_RG.ict

# No decodes
$TRANSMOGRIFY Gap-1-2.ict

# No decodes
$TRANSMOGRIFY Gap-1-2B.ict

# No/random decodes
$TRANSMOGRIFY Gap16Bit.ict

# No decodes
$TRANSMOGRIFY Gap24Bit.ict

# No decodes
$TRANSMOGRIFY GapBase4.ict
$TRANSMOGRIFY GlowShow.ict
$TRANSMOGRIFY "Grundig DTR-8860.ict"
$TRANSMOGRIFY Grundig16-30.ict
$TRANSMOGRIFY Grundig16.ict
$TRANSMOGRIFY GuangZhou.ict
$TRANSMOGRIFY "HUMAX iHD-FOX C (UEI).ict"
$TRANSMOGRIFY Humax4Phase.ict -f 6000
$TRANSMOGRIFY IODATA1.ict

# Unknown, no decodes
$TRANSMOGRIFY IR20140731203639.ict
$TRANSMOGRIFY JVC.ict
$TRANSMOGRIFY Jerrold.ict
$TRANSMOGRIFY Kaseikyo.ict
$TRANSMOGRIFY Kathrein.ict
$TRANSMOGRIFY Kathrein_VolUp.ict
$TRANSMOGRIFY Konka.ict

# Invalid, thus no decodes
$TRANSMOGRIFY Lumagen_Pronto.txt
$TRANSMOGRIFY Lumagen_Pronto_fixed.txt

# Serial protocol, some decodes
$TRANSMOGRIFY Lutron_Pronto.txt

# Some decodes
$TRANSMOGRIFY Manchester76bit.ict
$TRANSMOGRIFY Metz_Pronto.txt

# Some deocdes
$TRANSMOGRIFY Misubishi-K.ict

# Probably an AC control not in IrpProtocols. No decodes.
$TRANSMOGRIFY MisubishiAirCon_partial.ict
$TRANSMOGRIFY Mitsubishi-K.pronto --frequencytol -1
$TRANSMOGRIFY NEC-f16.ict
$TRANSMOGRIFY NEC.ict
$TRANSMOGRIFY NEC1-FDS.ict
$TRANSMOGRIFY NEC1-FDS_short.ict
$TRANSMOGRIFY NEC1-rnc.ict
$TRANSMOGRIFY NEC1-rnc_B.ict
$TRANSMOGRIFY NEC1-y1_nofinalA.ict
$TRANSMOGRIFY NEC1-y1_nofinal_B.ict
$TRANSMOGRIFY NEC1.ict
$TRANSMOGRIFY NEC2-f16.ict
$TRANSMOGRIFY NEC2-rnc.ict
$TRANSMOGRIFY NECx2_NECx1.ict
$TRANSMOGRIFY NRC17_C0723.ict
$TRANSMOGRIFY NRC17_C0723_OK.ict
$TRANSMOGRIFY Nokia32.ict

# No decodes
$TRANSMOGRIFY Nokia36_C3641.ict
$TRANSMOGRIFY "Nova Pace.ict"
$TRANSMOGRIFY "Ortek VRC-1100.ict"
$TRANSMOGRIFY OrtekMCE.ict
$TRANSMOGRIFY OrtekMCE_Power.ict
$TRANSMOGRIFY PCTV.ict
$TRANSMOGRIFY Pace.ict
$TRANSMOGRIFY Panasonic.ict
$TRANSMOGRIFY Panasonic2.ict
$TRANSMOGRIFY Panasonic_Old.ict
$TRANSMOGRIFY Panasonic_Old_Pronto.txt
$TRANSMOGRIFY PioneerMix.ict
$TRANSMOGRIFY PioneerMix2.ict
$TRANSMOGRIFY Proton.ict
$TRANSMOGRIFY RC-6-16.ict
$TRANSMOGRIFY RC5-7F-57_17.ict
$TRANSMOGRIFY RC5-7F.ict
$TRANSMOGRIFY RC5.ict
$TRANSMOGRIFY RC5x.ict
$TRANSMOGRIFY RC6-6-20.ict
$TRANSMOGRIFY RC6-6-24.ict
$TRANSMOGRIFY RC6-6-56.ict --frequencytolerance 3000
$TRANSMOGRIFY RC6-M-28n.ict
$TRANSMOGRIFY RC6.ict
$TRANSMOGRIFY RCA-38.ict
$TRANSMOGRIFY RCA_Old.ict
$TRANSMOGRIFY Replay.ict
$TRANSMOGRIFY Roku.ict
$TRANSMOGRIFY RossMIcro.txt
$TRANSMOGRIFY SIM2.txt
$TRANSMOGRIFY Sampo_T1755.ict
$TRANSMOGRIFY Samsung20.ict
$TRANSMOGRIFY Samsung36.ict
$TRANSMOGRIFY SciAtl-6.ict
$TRANSMOGRIFY Sejin-1-56.ict
$TRANSMOGRIFY SharpDVD.ict
$TRANSMOGRIFY Sharp_Pronto.txt

# No decodes
$TRANSMOGRIFY SolidTek16.ict

# No decodes
$TRANSMOGRIFY Solidtek16_2.ict

# No decodes
$TRANSMOGRIFY Solidtek16_key1.ict
$TRANSMOGRIFY Sony.ict
$TRANSMOGRIFY Sony12B.ict
$TRANSMOGRIFY Sony15B.ict
$TRANSMOGRIFY Sony20.ict
$TRANSMOGRIFY Sony20B.ict
$TRANSMOGRIFY Sony8B.ict
$TRANSMOGRIFY SonyAll.ict
$TRANSMOGRIFY Sony_15_20.ict
$TRANSMOGRIFY Sony_A2172.ict
$TRANSMOGRIFY Sunfire_0.ict
$TRANSMOGRIFY TDC-38_6_10.ict
$TRANSMOGRIFY TDC-56_14_10.ict
$TRANSMOGRIFY Teac_0_4.ict
$TRANSMOGRIFY Teac_0_4_Input.ict
$TRANSMOGRIFY Teac_0_4_VolUp.ict
$TRANSMOGRIFY Thomson-0625.ict
$TRANSMOGRIFY "Tivo Unit 0.ict"

# CHECK First signal erroneous, thus only two decodes
$TRANSMOGRIFY Velleman_Pronto.txt
$TRANSMOGRIFY ViewStar.ict
$TRANSMOGRIFY Vudu_C2298.ict

# No decodes
$TRANSMOGRIFY X-10ext.ict

# Just a few decodes
$TRANSMOGRIFY X10_0167_7980.ict
$TRANSMOGRIFY X10_0167_7980_ChUp.ict
$TRANSMOGRIFY XMP-1.ict
$TRANSMOGRIFY XMP-1_Display.ict

# No decodes
$TRANSMOGRIFY XMP-1_FF_1923.ict

# No decodes
$TRANSMOGRIFY XMP_1923_FinalFrame.ict
$TRANSMOGRIFY "Xiaomi MDZ-16-AA.ict" --frequencytolerance 2500
$TRANSMOGRIFY Zaptor-56.ict
$TRANSMOGRIFY Zaptor-56_shortsecondLO.ict
$TRANSMOGRIFY ZenithAll.ict --frequencytolerance 2500
$TRANSMOGRIFY Zenith_6_0_1.ict --frequencytolerance 2500
$TRANSMOGRIFY Zenith_7_0_10.ict --frequencytolerance 2500
$TRANSMOGRIFY glowsamples.ict

# No decodes, unknown protocol
$TRANSMOGRIFY jiangsu.ict
$TRANSMOGRIFY longjing.ict
$TRANSMOGRIFY longjingA.ict
$TRANSMOGRIFY mitsubishi.ict

# No decodes
$TRANSMOGRIFY unknownA.ict
$TRANSMOGRIFY velodyne.ict
