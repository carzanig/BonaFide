protocol RTSP
PFport 30003
RFport 31003

request string("rtsp/1.0 200 ok") byte(13) byte(10) string("CSeq: 1") byte(13) byte(10) string("Content-Base: rtsp://example.com/media.mp4") byte(13) byte(10) string("Content-Type: application/sdp") byte(13) byte(10) string("m=video 0 RTP/AVP 96") byte(13) byte(10) string("a=control:streamid=0") byte(13) byte(10) string("a=range:npt=0-7.741000") byte(13) byte(10) string("a=length:npt=7.741000") byte(13) byte(10) string("a=rtpmap:96 MP4V-ES/5544") byte(13) byte(10) string("") byte(13) byte(10) string("a=mimetype:string;"video/MP4V-ES"") byte(13) byte(10) string("a=AvgBitRate:integer;304018") byte(13) byte(10) string("a=StreamName:string;"hinted video track"") byte(13) byte(10) string("m=audio 0 RTP/AVP 97") byte(13) byte(10) string("a=control:streamid=1") byte(13) byte(10) string("a=range:npt=0-7.712000") byte(13) byte(10) string("a=length:npt=7.712000") byte(13) byte(10) string("a=rtpmap:97 mpeg4-generic/32000/2") byte(13) byte(10) string("a=mimetype:string;"audio/mpeg4-generic"") byte(13) byte(10) string("a=AvgBitRate:integer;65790") byte(13) byte(10) string("a=StreamName:string;"hinted audio track"") byte(13) byte(10) byte(13) byte(10)

response string("DESCRIBE rtsp://example.com/media.mp4 RTSP/1.0") byte(13) byte(10) string("CSeq: 1") byte(13) byte(10) byte(13) byte(10)
