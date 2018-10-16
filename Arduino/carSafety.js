var five = require("johnny-five");
var mqtt = require('mqtt');


const GREEN_LED = 4, YELLOW_LED = 3, RED_LED = 2, PIEZO = 5;

var board, leds, ping, piezo, tilt;


//Basic Flow

var client = mqtt.connect("mqtt://localhost:1883",{
	username:'mqtt-test',
	password:'mqtt-test'
});

board = new five.Board();

//callbacks

client.on("connect", function(){
	console.log("Successful connection");
	client.subscribe("alert", function(){
		console.log("Subscribed")})
})


client.on("message", function(topic, messsage){
	console.log("Accel Alert!");
	piezo.frequency(100, 500);
})

board.on("ready", function() {
 	
	leds = five.Leds([GREEN_LED, YELLOW_LED, RED_LED]);
	piezo = new five.Piezo(PIEZO);

	tilt_config();
	ping_config();

})


//Helpfull functions

function tilt_config(){

	tilt = new five.Sensor.Digital(7);
	
	tilt.on("change", function() {
		if (this.value == 1){
      		client.publish("carSafety", "alert", 1, function(){
					console.log("Crash Report")});
		}
  	});

}

function ping_config(){

	ping = new five.Proximity({
		controller: "HCSR04",
		pin: 6
	});

	ping.on("data", function(){
		data_handle(this.cm);
	});

}

function data_handle(dist){
	if (dist >= 10){
		leds[0].on();
		leds[1].off();
		leds[2].off();
		piezo.noTone();
	}
	else if (dist >= 5){
		leds[1].on();
		leds[0].off();
		leds[2].off();
		piezo.noTone();
	}else{
		leds[2].on();
		leds[0].off();
		leds[1].off();
		piezo.frequency(100, 200);	
	}
}









