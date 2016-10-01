#!/bin/sh

RUN="target/irptransmogrifier.sh recog -r -t -p"

${RUN} 48-nec1
${RUN} 48-nec2
${RUN} adnotam
# Silly ... ${RUN} airasync
${RUN} aiwa
${RUN} akai
${RUN} amino-56
${RUN} amino
# Repetitions causing non-interlacing ${RUN} anthem
${RUN} apple
${RUN} archer
${RUN} barco
${RUN} blaupunkt
${RUN} bose
${RUN} bryston
${RUN} canalsat
${RUN} canalsatld
${RUN} denon
${RUN} denon-k
${RUN} dgtec
${RUN} directv
${RUN} dishplayer
${RUN} dish_network
${RUN} elan
${RUN} emerson
${RUN} f12
${RUN} f32
${RUN} fujitsu
${RUN} fujitsu-56
${RUN} g.i.cable
${RUN} g.i.4dtv
${RUN} 'gi rg'
# hard... ${RUN} grundig16
# hard... ${RUN} grundig16-30
${RUN} gxb
# hard... ${RUN} 'humax 4phase'
${RUN} iodatan
${RUN} jerrold
${RUN} jvc
${RUN} jvc-48
${RUN} jvc-56
${RUN} kaseikyo
${RUN} kaseikyo56
${RUN} kathrein
${RUN} konka
${RUN} logitech
${RUN} lumagen
${RUN} matsui
# todo ${RUN} mce
${RUN} metz19
${RUN} mitsubishi
${RUN} mitsubishi-k
${RUN} nec1
${RUN} nec1-rnc
${RUN} nec1-f16
${RUN} nec2
${RUN} necx1
${RUN} necx2
${RUN} nokia
${RUN} nokia12
# todo ${RUN} nokia32
${RUN} nrc17
${RUN} ortekmce
${RUN} pacemss
${RUN} panasonic
${RUN} panasonic2
${RUN} panasonic_old
# non-interlacing ${RUN} pctv
${RUN} pid-0001
${RUN} pid-0003
${RUN} pid-0004
${RUN} pid-0083
${RUN} pioneer
${RUN} proton
${RUN} rc5
#${RUN} rc5-7f
${RUN} rc5-7f-57
#${RUN} rc5x
#${RUN} rc6
#${RUN} rc6-6-20
#${RUN} 'rca(old)'
${RUN} rca
${RUN} rca-38
#${RUN} 'rca-38(old)'
${RUN} recs80
${RUN} recs80-0045
${RUN} recs80-0068
${RUN} recs80-0090
${RUN} replay
${RUN} revox
${RUN} samsung20
#${RUN} samsung36
${RUN} sampo
${RUN} scatl-6
${RUN} sharp
${RUN} sharp{1}
${RUN} sharp{2}
${RUN} sharpdvd
${RUN} sim2
#${RUN} solidtek16
${RUN} somfy
${RUN} sony8
${RUN} sony12
${RUN} sony15
${RUN} sony20
${RUN} streamzap
${RUN} streamzap-57
${RUN} sunfire
${RUN} tdc-38
${RUN} tdc-56
${RUN} teac-k
${RUN} thomson
${RUN} thomson7
${RUN} tivo
${RUN} velleman
#${RUN} velodyne
${RUN} viewstar
${RUN} x10
${RUN} x10.n
#${RUN} xmpmeta
#${RUN} xmp
#${RUN} xmp-1
#${RUN} xmp-2
#${RUN} xmpfinalframemeta
#${RUN} xmpff
#${RUN} xmpff-1
#${RUN} xmpff-2
#${RUN} zaptor-36
#${RUN} zaptor-56
#${RUN} zenith
${RUN} canon
${RUN} arctech
${RUN} arctech-38
#${RUN} rs200
#${RUN} gwts
#${RUN} rc6-m-56
#${RUN} entone
${RUN} pioneer-mix
# Solving systems of equations.... #${RUN} fujitsu_aircon
${RUN} roku-official
