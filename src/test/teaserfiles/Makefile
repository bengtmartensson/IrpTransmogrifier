IRPTRANSMOGRIFIER := ../../../tools/irptransmogrifier --quiet --min-leadout 10000
ICTPARSE := ../../../contributed/input-filters/ict.sh
DIFF := diff -w

NORMAL_TESTS := \
Aiwa2_Aiwa \
Aiwa_left \
Amino_0_0 \
Amino_0_0_36K \
Anthem \
Apple \
Apple2 \
Apple2_Pause \
Apple_Down \
Audiovox_V0642 \
BO_0620 \
BOarrowUp \
BOnum5 \
BOvolUp \
BandO \
Base4_26Bit \
Blaupunkt \
Blaupunkt_Chplus \
Bose \
CanalSat-38 \
CanalSat-38_3 \
CanalSat \
CanalSatLD \
DIshNetwork_0775 \
Denon-K_Denon \
Denon \
Denon_left \
Denon_short \
Dgtec \
Dgtec2 \
Digivision_DVT-3025EU \
Digivison_DVT-3025EU_Two \
DirecTV_Pronto \
DishPlayer \
Dish_Network \
Dysan_Pronto \
Elan \
Emerson_0282 \
EpsonPowerLiteEMP835 \
EpsonPowerLiteEMP835_svideo \
EpsonPowerLite_Pronto \
Epson_8100 \
Epson_UnkProj \
F12-shortLO \
F12 \
Fujitsu_pronto \
GI_Cable \
GI-4DTV \
GI_RG \
Gap-1-2 \
Gap-1-2B \
Gap16Bit \
Gap24Bit \
GapBase4 \
GlowShow \
Grundig_DTR-8860 \
Grundig16-30 \
Grundig16 \
GuangZhou \
HUMAX_iHD-FOX_C__UEI_ \
Humax4Phase \
IODATA1 \
JVC \
Jerrold \
Kaseikyo \
Kathrein \
Kathrein_VolUp \
Konka \
Lumagen_Pronto \
Lumagen_Pronto_fixed \
Lutron_Pronto \
Manchester76bit \
Metz_Pronto \
Misubishi-K \
MisubishiAirCon_partial \
Mitsubishi-K.pronto \
NEC-f16 \
NEC \
NEC1-FDS \
NEC1-FDS_short \
NEC1-rnc \
NEC1-rnc_B \
NEC1-y1_nofinalA \
NEC1-y1_nofinal_B \
NEC1 \
NEC2-f16 \
NEC2-rnc \
NECx2_NECx1 \
NRC17_C0723 \
NRC17_C0723_OK \
Nokia32 \
Nokia36_C3641 \
Nova_Pace \
Ortek_VRC-1100 \
OrtekMCE \
OrtekMCE_Power \
PCTV \
Pace \
Panasonic \
Panasonic2 \
Panasonic_Old \
Panasonic_Old_Pronto \
PioneerMix \
PioneerMix2 \
Proton \
RC-6-16 \
RC5-7F-57_17 \
RC5-7F \
RC5 \
RC5x \
RC6-6-20 \
RC6-6-24 \
RC6-6-56 \
RC6-M-28n \
RC6 \
RCA-38 \
RCA_Old \
Replay \
Roku \
RossMIcro \
SIM2 \
Sampo_T1755 \
Samsung20 \
Samsung36 \
SciAtl-6 \
Sejin-1-56 \
SharpDVD \
Sharp_Pronto \
SolidTek16 \
Solidtek16_2 \
Solidtek16_key1 \
Sony \
Sony12B \
Sony15B \
Sony20 \
Sony20B \
Sony8B \
SonyAll \
Sony_15_20 \
Sony_A2172 \
Sunfire_0 \
TDC-38_6_10 \
TDC-56_14_10 \
Teac_0_4 \
Teac_0_4_Input \
Teac_0_4_VolUp \
Thomson-0625 \
Tivo_Unit_0 \
Velleman_Pronto \
ViewStar \
Vudu_C2298 \
X-10ext \
X10_0167_7980 \
X10_0167_7980_ChUp \
XMP-1 \
XMP-1_Display \
XMP-1_FF_1923 \
XMP_1923_FinalFrame \
Xiaomi_MDZ-16-AA \
Zaptor-56 \
Zaptor-56_shortsecondLO \
ZenithAll \
Zenith_6_0_1 \
Zenith_7_0_10 \
glowsamples \
jiangsu \
longjing \
longjingA \
mitsubishi \
unknownA \
velodyne

FREQTOLERANCE_CanalSatLD          := --frequencytolerance -1
FREQTOLERANCE_CanalSat_38         := --frequencytolerance -1
FREQTOLERANCE_CanalSat_38_3       := --frequencytolerance -1
FREQTOLERANCE_Elan                := --frequencytolerance -1
FREQTOLERANCE_Mitsubishi_K.pronto := --frequencytolerance -1
FREQTOLERANCE_Denon_short         := --frequencytolerance 4000
FREQTOLERANCE_Humax4Phase         := --frequencytolerance 6000
FREQTOLERANCE_RC6_6_56            := --frequencytolerance 6000
FREQTOLERANCE_Xiaomi_MDZ_16_AA    := --frequencytolerance 2500
FREQTOLERANCE_ZenithAll           := --frequencytolerance 2500
FREQTOLERANCE_Zenith_6_0_1        := --frequencytolerance 2500
FREQTOLERANCE_Zenith_7_0_10       := --frequencytolerance 2500
#FREQTOLERANCE_PioneerMix	  := --frequencytolerance -1

TARGETS := $(foreach f,$(NORMAL_TESTS),$(f).diff)

default: $(TARGETS)

%.diff: %.out | %.exp
	-$(DIFF) "$*.out" "$*.exp" > "$@"

%.out: %.txt
	$(IRPTRANSMOGRIFIER) $(FREQTOLERANCE_$(subst -,_,$(basename $@))) --out "$@" --tsv decode --recursive  --namedinput "$<"

ifneq ($(MAKE_EXP),)
%.exp: %.txt
	$(IRPTRANSMOGRIFIER) $(FREQTOLERANCE_$(subst -,_,$(basename $@))) --out "$@" --tsv decode --recursive  --namedinput "$<"
endif

%.txt: %.ict
	$(ICTPARSE) "$<" > "$@"

%.diffuse: %.out | %.exp
	diffuse $< $*.exp

nukeempty:
	find . -name \*.diff -size 0 -exec rm \{\} \;
	-ls -l *.diff

clean:
	rm -f *.diff *.out

.PHONY: nukeempty clean
