protocol BitTorrent
PFport 30007
RFport 31007

request byte(19) string("BitTorrent protocol") repbyte(0,5) byte(16) byte(0) byte(4) byte(197) string("hi") byte(225) byte(132) byte(136) byte(30) byte(247) byte(164) string("E") byte(136) string(":6") byte(244) string("_ZC") byte(209) byte(223) string("K-TR1930-5blq7inl915x")

response byte(19) string("BitTorrent protocol") repbyte(0,5) byte(16) byte(0) byte(5) byte(197) string("hi") byte(225) byte(132) byte(136) byte(30) byte(247) byte(164) string("E") byte(136) string(":6") byte(244) string("_ZC") byte(209) byte(223) string("K-UT3120-") byte(129) string("h") byte(245) byte(223) byte(238) string("6cRX") byte(15) string("y") byte(185) repbyte(0,3) byte(245) byte(20) byte(0) string("d1:ei0e4:ipv44:.") byte(148) byte(134) byte(174) string("4:i")

request repbyte(0,3) byte(162) byte(5) repbyte(0,161)
response repbyte(0,3) byte(162) byte(5) repbyte(255,161)

request repbyte(0,3) byte(1) byte(2)
response repbyte(0,3) byte(1) byte(1)



