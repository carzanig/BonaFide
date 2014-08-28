protocol HTTP
PFport 30008
RFport 31008

request string("GET /wiki/Jacobs_University_Bremen HTTP/1.1") byte(13) byte(10) string("Host: en.wikipedia.org") byte(13) byte(10) string("User-Agent: Mozilla/5.0 (Linux; U; Android 2.3.5; en-de; HTC Desire S Build/GRJ90) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1") byte(13) byte(10) string("Accept: text/html") byte(13) byte(10) string("Connection: close") byte(13) byte(10) byte(13) byte(10)

response string("HTTP/1.1 200 OK") byte(13) byte(10) string("Server: Apache") byte(13) byte(10) string("Content-Language: en") byte(13) byte(10) string("Content-type: text/html; charset=utf-8") byte(13) byte(10) string("Content-Length: 20") byte(13) byte(10) byte(13) byte(10) string("12345678901234567890")
