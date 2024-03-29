package org.harctoolbox.lirc;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.irp.NonUniqueBitCodeException;
import org.harctoolbox.irp.Protocol;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class LircIrpNGTest {

    private final static String MODE2_STRING = "begin remote \n"
            + "\n"
            + "  name  yamaha-amp\n"
            + "  bits           16\n"
            + "  flags SPACE_ENC\n"
            + "  eps            40\n"
            + "  aeps          100\n"
            + "\n"
            + "  header       9067  4393\n"
            + "  one           642   470\n"
            + "  zero          642  1600\n"
            + "  ptrail        642\n"
            + "  repeat       9065  2139\n"
            + "  pre_data_bits   16\n"
            + "  pre_data       0xA15E\n"
            + "  gap          39597\n"
            + "  repeat_bit      0\n"
            + "\n"
            + "      begin codes\n"
            + "          KEY_MUTE                 0x000000000000C738        #  Was: mute\n"
            + "          volume^                  0x000000000000A758\n"
            + "          KEY_VOLUMEDOWN           0x00000000000027D8        #  Was: volume-\n"
            + "          KEY_POWER                0x00000000000047B8        #  Was: on\n"
            + "          standby                  0x0000000000008778        #  Was: standby\n"
            + "          effect                   0x000000000000956A\n"
            + "          decoder                  0x0000000000001EE1\n"
            + "          phono                    0x000000000000D728\n"
            + "          v-aux                    0x00000000000055AA\n"
            + "          vcr2                     0x00000000000037C8\n"
            + "          tv/dbs                   0x000000000000D52A\n"
            + "          KEY_DVD                  0x00000000000017E8        #  Was: dvd/ld\n"
            + "          vcr1                     0x0000000000000FF0\n"
            + "          KEY_TUNER                0x0000000000009768        #  Was: tuner\n"
            + "          KEY_CD                   0x00000000000057A8        #  Was: cd\n"
            + "          tape/md                  0x000000000000E718\n"
            + "          KEY_1                    0x000000000000EE11        #  Was: 1\n"
            + "          KEY_2                    0x0000000000006E91        #  Was: 2\n"
            + "          KEY_3                    0x000000000000AE51        #  Was: 3\n"
            + "          KEY_4                    0x0000000000002ED1        #  Was: 4\n"
            + "          KEY_5                    0x000000000000CE31        #  Was: 5\n"
            + "          KEY_6                    0x0000000000004EB1        #  Was: 6\n"
            + "          KEY_7                    0x0000000000008E71        #  Was: 7\n"
            + "          KEY_8                    0x0000000000000EF1        #  Was: 8\n"
            + "          KEY_9                    0x000000000000F609        #  Was: 9\n"
            + "          KEY_0                    0x0000000000007689        #  Was: 0\n"
            + "          ^                        0x000000000000B748\n"
            + "          tuner+                   0x000000000000F708\n"
            + "          tuner-                   0x0000000000007788\n"
            + "          test                     0x0000000000005EA1\n"
            + "          level                    0x0000000000009E61\n"
            + "          KEY_SLEEP                0x00000000000015EA        #  Was: sleep\n"
            + "          KEY_EPG                  0x000000000000BC43        #  Was: onscreen\n"
            + "          set+                     0x0000000000008679\n"
            + "          set-                     0x00000000000006F9\n"
            + "          set^                     0x00000000000046B9\n"
            + "          setV                     0x000000000000C639\n"
            + "          parameter^               0x0000000000005CA3\n"
            + "          parameterV               0x000000000000DC23\n"
            + "          parameter-               0x0000000000001CE3\n"
            + "          parameter+               0x0000000000009C63\n"
            + "      end codes\n"
            + "\n"
            + "end remote";

    private static final String RAW_STRING = "#\n"
            + "# this config file was automatically generated\n"
            + "# using lirc-0.7.0(serial) on Thu Jul  7 21:36:22 2005\n"
            + "#\n"
            + "# contributed by aaron\n"
            + "#\n"
            + "# brand:                       Zenith\n"
            + "# model no. of remote control: ZN110 (single universal remote)\n"
            + "# devices being controlled by this remote: universal\n"
            + "#\n"
            + "# NOTE:  I don't think the sleep button works\n"
            + "#\n"
            + " \n"
            + "\n"
            + "begin remote\n"
            + "\n"
            + "  name   Zenith_ZD110\n"
            + "  flags RAW_CODES\n"
            + "  eps            30\n"
            + "  aeps          100\n"
            + "\n"
            + "  ptrail          0\n"
            + "  repeat  9053  2168\n"
            + "  gap    120930\n"
            + "\n"
            + "      begin raw_codes\n"
            + "\n"
            + "          name CH_UP\n"
            + "             9100    4393     658     439     660    1584\n"
            + "              654    1579     658     453     660     464\n"
            + "              661     439     660     464     663    1574\n"
            + "              663    1564     662    1575     661     464\n"
            + "              662    1574     664     435     664    1589\n"
            + "              662    1574     663     437     662     462\n"
            + "              663     436     663     450     661    1574\n"
            + "              663    1577     662     463     663    1561\n"
            + "              662     450     663    1574     663    1576\n"
            + "              662    1588     663     436     661     464\n"
            + "              664    1573     663     436     663    1575\n"
            + "              662   41801    9074    2152     658\n"
            + "\n"
            + "          name CH_DOWN\n"
            + "             9124    4369     659     466     657    1579\n"
            + "              658    1583     655     442     657     453\n"
            + "              663     461     668     431     661    1577\n"
            + "              663    1574     663    1589     663     436\n"
            + "              659    1578     663     462     662    1563\n"
            + "              663    1575     661     464     662    1575\n"
            + "              662    1563     663     460     663     436\n"
            + "              663     461     662     437     663    1575\n"
            + "              665     462     663     436     661     464\n"
            + "              662    1574     661    1564     662    1574\n"
            + "              663    1575     663     464     660    1578\n"
            + "              662   41800    9047    2155     655\n"
            + "\n"
            + "          name VOL_UP\n"
            + "              696     291     642    3926     621    4920\n"
            + "              619     356     622    3935     619     358\n"
            + "              619    3937     621    4932     597    4944\n"
            + "              597     367     619    3951     595     367\n"
            + "              618    3952     596    4946     595    4932\n"
            + "              621     367     596\n"
            + "\n"
            + "          name VOL_DOWN\n"
            + "              638     326     641    3948     597    4945\n"
            + "              596     367     622    3949     595    4946\n"
            + "              596     381     594    3962     595    4935\n"
            + "              619     366     596    3951     618     369\n"
            + "              594    3951     621    4919     621    4923\n"
            + "              619     355     619\n"
            + "\n"
            + "          name 0\n"
            + "             9108    4370     660     466     657    1581\n"
            + "              660    1565     660     464     658     441\n"
            + "              660     463     670     431     670    1581\n"
            + "              679    1556     681    1557     683     415\n"
            + "              684    1568     688     411     686    1552\n"
            + "              690    1549     688     435     690     423\n"
            + "              690     409     690    1546     693     432\n"
            + "              691    1547     693     420     693     406\n"
            + "              690     420     693    1547     691    1558\n"
            + "              693     406     692    1546     692     432\n"
            + "              693    1545     693    1532     693    1559\n"
            + "              690   41760    9103    2109     677\n"
            + "\n"
            + "          name 1\n"
            + "             9149    4378     660     453     658    1580\n"
            + "              657    1580     657     442     660     464\n"
            + "              661     452     661     436     661    1586\n"
            + "              653    1577     660    1590     662     436\n"
            + "              663    1574     663     462     663    1575\n"
            + "              663    1562     663     462     664     436\n"
            + "              662     462     663    1582     654     451\n"
            + "              662     437     663     459     674    1566\n"
            + "              662     448     663    1563     663    1576\n"
            + "              662     462     662    1575     663    1563\n"
            + "              663    1574     663     462     662    1575\n"
            + "              670   41794    9048    2151     659\n"
            + "\n"
            + "          name 2\n"
            + "             9133    4335     712     413     709    1552\n"
            + "              688    1527     713     399     711     388\n"
            + "              712     413     711     388     711    1547\n"
            + "              692    1559     690    1525     716     383\n"
            + "              714    1525     714     431     693    1511\n"
            + "              716    1521     716     408     716    1522\n"
            + "              716     397     716    1521     716     384\n"
            + "              715     409     716     383     716    1536\n"
            + "              716     418     692     383     716    1545\n"
            + "              692     410     715    1510     716    1521\n"
            + "              716    1545     693     409     716    1521\n"
            + "              716   41747    9101    2130     669\n"
            + "\n"
            + "          name 3\n"
            + "             9147    4357     691     387     712    1570\n"
            + "              681    1547     688     388     714     411\n"
            + "              713     388     712     410     714    1535\n"
            + "              689    1526     713    1525     712     413\n"
            + "              712    1533     691     413     711    1527\n"
            + "              708    1528     710     424     691     388\n"
            + "              711    1547     689    1533     701     439\n"
            + "              688     404     711     386     712    1528\n"
            + "              706     427     686    1542     705     416\n"
            + "              685     419     706    1530     706    1553\n"
            + "              677    1528     695     430     698    1548\n"
            + "              672   41782    9100    2121     675\n"
            + "\n"
            + "          name 4\n"
            + "             9111    4362     667     455     670    1567\n"
            + "              670    1570     667     430     669     444\n"
            + "              670     454     669     430     669    1569\n"
            + "              670    1568     669    1581     670     430\n"
            + "              667    1570     670     455     670    1555\n"
            + "              670    1568     667     458     669    1567\n"
            + "              669    1557     668    1568     670     457\n"
            + "              667     432     668     456     667    1557\n"
            + "              670     443     668     456     670     429\n"
            + "              667     457     668    1557     669    1568\n"
            + "              670    1567     670     457     668    1569\n"
            + "              670   41794    9053    2146     668\n"
            + "\n"
            + "          name 5\n"
            + "             9113    4380     647     457     668    1570\n"
            + "              668    1555     670     454     671     430\n"
            + "              669     455     669     430     669    1567\n"
            + "              670    1582     670    1568     670     428\n"
            + "              670    1568     670     443     670    1568\n"
            + "              667    1569     671     454     670     430\n"
            + "              669     444     667     458     666    1570\n"
            + "              668     429     670     457     667    1557\n"
            + "              670     442     667    1571     667    1571\n"
            + "              669    1581     668     430     669    1568\n"
            + "              669    1571     667     443     670    1581\n"
            + "              671   41793    9055    2146     667\n"
            + "\n"
            + "          name 6\n"
            + "             9086    4372     668     457     667    1558\n"
            + "              669    1568     669     454     671     430\n"
            + "              669     454     671     442     670    1573\n"
            + "              665    1567     670    1568     670     443\n"
            + "              670    1566     670     430     670    1568\n"
            + "              669    1582     670     429     670    1567\n"
            + "              670     455     670     429     670    1581\n"
            + "              670     427     680     445     670    1567\n"
            + "              671     443     668     443     670    1569\n"
            + "              667    1570     670     430     669    1581\n"
            + "              670    1568     667     433     666    1582\n"
            + "              679   41772    9082    2143     669\n"
            + "\n"
            + "          name 7\n"
            + "             9133    4347     669     454     669    1558\n"
            + "              669    1568     670     454     669     427\n"
            + "              675     454     678     418     681    1570\n"
            + "              677    1560     676    1562     681     418\n"
            + "              683    1568     678     421     688    1550\n"
            + "              686    1551     689     424     690     434\n"
            + "              693    1545     691     408     693    1545\n"
            + "              693    1557     693     407     693    1546\n"
            + "              691     418     695    1557     694     404\n"
            + "              697    1541     695     429     695     404\n"
            + "              695    1557     695     404     695    1554\n"
            + "              697   41755    9109    2116     686\n"
            + "\n"
            + "          name 8\n"
            + "             9133    4345     683     428     688    1550\n"
            + "              681    1557     683     441     689     410\n"
            + "              691     422     688     437     687    1550\n"
            + "              686    1551     689    1537     686     444\n"
            + "              683    1550     685     413     686    1552\n"
            + "              683    1568     684     413     687    1553\n"
            + "              685     439     688    1538     682     441\n"
            + "              687    1549     686     413     687     441\n"
            + "              685     414     686     435     689    1551\n"
            + "              682     415     689    1551     679     431\n"
            + "              686    1552     676    1562     679    1561\n"
            + "              676   41787    9107    2116     670\n"
            + "\n"
            + "          name 9\n"
            + "             9131    4339     695     403     696    1565\n"
            + "              671    1557     695     404     696     429\n"
            + "              695     425     673     431     694    1542\n"
            + "              696    1552     674    1563     674     427\n"
            + "              700    1540     695     439     677    1560\n"
            + "              681    1557     674     402     707    1567\n"
            + "              679     396     710     415     707    1554\n"
            + "              684     392     709     415     712     401\n"
            + "              709     402     712     387     712    1526\n"
            + "              709    1542     708     391     716    1547\n"
            + "              686    1525     712    1541     710    1516\n"
            + "              710   41751    9105    2143     686\n"
            + "\n"
            + "          name ENTER\n"
            + "             9129    4364     672     424     675    1563\n"
            + "              674    1566     672     438     675     450\n"
            + "              674     404     695     429     695    1564\n"
            + "              674    1552     674    1563     674     450\n"
            + "              675    1542     695     402     698    1576\n"
            + "              674    1564     674     425     673    1565\n"
            + "              673    1578     671     405     695    1543\n"
            + "              694    1565     672     441     672    1543\n"
            + "              695     416     697     429     695     402\n"
            + "              710    1564     673     404     695     432\n"
            + "              692    1566     673     403     694    1566\n"
            + "              675   41767    9107    2141     681\n"
            + "\n"
            + "          name POWER\n"
            + "             9100    4392     658     441     658    1580\n"
            + "              657    1580     661     463     660     453\n"
            + "              660     437     663     461     664    1574\n"
            + "              663    1577     660    1563     663     462\n"
            + "              663    1574     663     436     663    1588\n"
            + "              663    1576     662     436     662     462\n"
            + "              664    1574     663     450     663     413\n"
            + "              686    1575     662     462     663     437\n"
            + "              675     449     663    1573     664     413\n"
            + "              686    1575     662    1590     662     413\n"
            + "              686    1551     686    1575     663    1575\n"
            + "              662   41780    9098    2149     658\n"
            + "\n"
            + "          name CC\n"
            + "             9111    4370     670     443     668    1568\n"
            + "              670    1567     670     431     668     455\n"
            + "              670     443     670     430     669    1567\n"
            + "              669    1569     669    1582     670     429\n"
            + "              669    1568     670     455     668    1570\n"
            + "              669    1556     668     457     667    1570\n"
            + "              668    1569     668    1560     663    1573\n"
            + "              666    1572     664    1595     657    1573\n"
            + "              664    1558     666     459     665     448\n"
            + "              665     434     663     462     663     437\n"
            + "              662     461     663     436     663     462\n"
            + "              663   41801    9080    2118     697\n"
            + "\n"
            + "          name CH_PREV\n"
            + "             9111    4348     710     414     711    1516\n"
            + "              710    1526     711     413     712     388\n"
            + "              714     411     711     423     690    1523\n"
            + "              717    1523     714    1545     692     400\n"
            + "              713    1522     715     384     717    1521\n"
            + "              715    1535     717     383     715    1523\n"
            + "              715    1521     717    1535     716    1521\n"
            + "              715     392     705     412     713    1524\n"
            + "              714     388     711     411     714     385\n"
            + "              713     411     714     385     714    1538\n"
            + "              711    1547     690     388     712    1561\n"
            + "              685   41743    9109    2140     673\n"
            + "\n"
            + "          name MUTE\n"
            + "              621     355     612    3949     607    4943\n"
            + "              603     362     601    3967     607     379\n"
            + "              581    3964     610    4933     607    4916\n"
            + "              628     373     589    3941     632     374\n"
            + "              587    3939     632    4941     594     369\n"
            + "              612    3958     593\n"
            + "\n"
            + "      end raw_codes\n"
            + "\n"
            + "end remote";

    private static final String LIRCMODE_STRING = "#\n"
            + "# this config file was automatically generated\n"
            + "# using lirc-0.7.0-CVS(atiusb) on Tue Apr 27 23:51:09 2004\n"
            + "#\n"
            + "# contributed by Paul Miller <pmiller9@users.sourceforge.net>\n"
            + "#\n"
            + "# brand: ATI Remote Wonder\n"
            + "# model no. of remote control: 5000015900A\n"
            + "# devices being controlled by this remote: ATI USB Receiver\n"
            + "#\n"
            + "# CHANNEL CODES\n"
            + "# To change your channel, hold the hand button down until the\n"
            + "# LED begins to blink.  Then enter the channel number\n"
            + "# (1 through 16) and press the hand again.\n"
            + "#\n"
            + "# NOTE!! The lirc_atiusb driver now removes the channel code\n"
            + "# from key-codes (by default).  This effectively outputs codes\n"
            + "# for remote channel 1.  You can change this behavior by\n"
            + "# loading the module with unique=1.  Type `modinfo lirc_atiusb`\n"
            + "# for details.\n"
            + "\n"
            + "begin remote\n"
            + "\n"
            + "  name  ATIUSB_5000015900A\n"
            + "  bits           16\n"
            + "  eps            30\n"
            + "  aeps          100\n"
            + "\n"
            + "  one             0     0\n"
            + "  zero            0     0\n"
            + "  pre_data_bits   8\n"
            + "  pre_data       0x14\n"
            + "  post_data_bits  16\n"
            + "  post_data      0x0\n"
            + "  gap          139944\n"
            + "  toggle_bit      0\n"
            + "\n"
            + "\n"
            + "      begin codes\n"
            + "          KEY_A                    0x000000000000F500        #  Was: A\n"
            + "          KEY_B                    0x000000000000F601        #  Was: B\n"
            + "          KEY_C                    0x0000000000000E19        #  Was: C\n"
            + "          KEY_D                    0x000000000000101B        #  Was: D\n"
            + "          KEY_E                    0x0000000000001621        #  Was: E\n"
            + "          KEY_F                    0x0000000000001823        #  Was: F\n"
            + "          KEY_TV                   0x000000000000F803        #  Was: TV\n"
            + "          KEY_DVD                  0x000000000000F904        #  Was: DVD\n"
            + "          KEY_WWW                  0x000000000000FA05        #  Was: WEB\n"
            + "          BOOK                     0x000000000000FB06\n"
            + "          HAND                     0x000000000000FC07\n"
            + "          KEY_POWER                0x000000000000F702        #  Was: POWER\n"
            + "          BTN_LEFT                 0x0000000000006D78        #  Was: MOUSE_LEFT_BTN\n"
            + "          BTN_RIGHT                0x000000000000717C        #  Was: MOUSE_RIGHT_BTN\n"
            + "          MOUSE_NORTH              0x0000000000006772        #  Was: MOUSE_UP\n"
            + "          MOUSE_SOUTH              0x0000000000006B76        #  Was: MOUSE_DOWN\n"
            + "          MOUSE_WEST               0x0000000000006C77        #  Was: MOUSE_LEFT\n"
            + "#          MOUSE_EAST               0x0000000000006B76        #  Was: MOUSE_RIGHT   duplicate\n"
            + "          KEY_VOLUMEUP             0x000000000000FD08        #  Was: VOL_UP\n"
            + "          KEY_VOLUMEDOWN           0x000000000000FE09        #  Was: VOL_DOWN\n"
            + "          KEY_MUTE                 0x000000000000FF0A        #  Was: MUTE\n"
            + "          KEY_CHANNELUP            0x000000000000000B        #  Was: CH_UP\n"
            + "          KEY_CHANNELDOWN          0x000000000000010C        #  Was: CH_DOWN\n"
            + "          KEY_1                    0x000000000000020D        #  Was: 1\n"
            + "          KEY_2                    0x000000000000030E        #  Was: 2\n"
            + "          KEY_3                    0x000000000000040F        #  Was: 3\n"
            + "          KEY_4                    0x0000000000000510        #  Was: 4\n"
            + "          KEY_5                    0x0000000000000611        #  Was: 5\n"
            + "          KEY_6                    0x0000000000000712        #  Was: 6\n"
            + "          KEY_7                    0x0000000000000813        #  Was: 7\n"
            + "          KEY_8                    0x0000000000000914        #  Was: 8\n"
            + "          KEY_9                    0x0000000000000A15        #  Was: 9\n"
            + "          KEY_0                    0x0000000000000C17        #  Was: 0\n"
            + "          KEY_LIST                 0x0000000000000B16        #  Was: LIST\n"
            + "          CHECK                    0x0000000000000D18\n"
            + "          KEY_UP                   0x0000000000000F1A        #  Was: UP\n"
            + "          KEY_DOWN                 0x0000000000001722        #  Was: DOWN\n"
            + "          KEY_LEFT                 0x000000000000121D        #  Was: LEFT\n"
            + "          KEY_RIGHT                0x000000000000141F        #  Was: RIGHT\n"
            + "          KEY_OK                   0x000000000000131E        #  Was: OK\n"
            + "          TIMER                    0x000000000000111C\n"
            + "          KEY_MAX                  0x0000000000001520        #  Was: MAX\n"
            + "          KEY_REWIND               0x0000000000001924        #  Was: REWIND\n"
            + "          KEY_PLAY                 0x0000000000001A25        #  Was: PLAY\n"
            + "          KEY_FASTFORWARD          0x0000000000001B26        #  Was: FFWD\n"
            + "          KEY_RECORD               0x0000000000001C27        #  Was: REC\n"
            + "          KEY_STOP                 0x0000000000001D28        #  Was: STOP\n"
            + "          KEY_PAUSE                0x0000000000001E29        #  Was: PAUSE\n"
            + "      end codes\n"
            + "\n"
            + "end remote";

    private final LircRemote lircRemote;
    private final LircRemote rawRemote;
    private final LircRemote lircModeRemote;

    public LircIrpNGTest() throws IOException {
        StringReader reader = new StringReader(MODE2_STRING);
        lircRemote = LircConfigFile.readRemotes(reader).get(0);
        StringReader rawReader = new StringReader(RAW_STRING);
        rawRemote = LircConfigFile.readRemotes(rawReader).get(0);
        StringReader lircModeReader = new StringReader(LIRCMODE_STRING);
        lircModeRemote = LircConfigFile.readRemotes(lircModeReader).get(0);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toProtocol method, of class LircIrp.
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testToProtocol() {
        System.out.println("toProtocol");
        Protocol result;
        try {
            result = LircIrp.toProtocol(lircRemote);
            assertEquals(result.toIrpString(), "{38.0k,1,msb}<642u,-1600u|642u,-470u>(9067u,-4393u,pre_data:16,F:16,642u,-39597u,(9065u,-2139u,642u,-39597u)*){pre_data=41310}[F:0..65535]");
        } catch (LircIrp.RawRemoteException | LircIrp.LircCodeRemoteException | NonUniqueBitCodeException ex) {
            Logger.getLogger(LircIrpNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            LircIrp.toProtocol(rawRemote);
            fail();
        } catch (LircIrp.RawRemoteException ex) {
        } catch (LircIrp.LircCodeRemoteException | NonUniqueBitCodeException ex) {
            fail();
        }

        try {
            LircIrp.toProtocol(lircModeRemote);
            fail();
        } catch (LircIrp.RawRemoteException | NonUniqueBitCodeException ex) {
            fail();
        } catch (LircIrp.LircCodeRemoteException ex) {
        }
    }
}
