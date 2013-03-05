var http = require('http');

var client;
if (process.env.REDISTOGO_URL) {
	var rtg   = require("url").parse(process.env.REDISTOGO_URL);
	var client = require("redis").createClient(rtg.port, rtg.hostname);
	client.auth(rtg.auth.split(":")[1]);
} else {
  var client = require("redis").createClient();
}

client.on("error", function (err) {
    console.log("Error " + err);
});

var port = process.env.PORT || 5000;
http.createServer(function (req, res) {
  	res.writeHead(200, {'Content-Type': 'application/json'});
	client.smembers("testgroups", function(err, testGroups){
		function getRandomVariant(i, returnVariants){
			client.hgetall(testGroups[i], function(err, variants){
				if(i >=0){
					var random = Math.floor(Math.random() * 100);
					for(variant in variants){
						random -= variants[variant];
						if(random < 0){
							returnVariants.push(variant);
							break;
						}
					}
					getRandomVariant(i-1, returnVariants);				
				}else{
					res.write(JSON.stringify(returnVariants));
					res.end();
				}
			});
		}// End of getRandomVariant
		var randomVariants = []
		getRandomVariant(testGroups.length-1, randomVariants);
	});//End of sMembers
}).listen(port);
console.log('Server running at http://127.0.0.1:5000/');


