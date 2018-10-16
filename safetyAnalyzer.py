import pika
import json
import sys


def carSafety_callback(ch, method, properties, body):
	print("Car crashed")
	ch.basic_ack(delivery_tag = method.delivery_tag)
	ch.basic_consume(passengerSafety_callback, queue = 'accel_data', no_ack = True)
	ch.basic_publish(exchange = 'amq.topic', routing_key = 'android_rpc', body ='send')
	

def passengerSafety_callback(ch, method, properties, body):
	data = body.decode('utf-8').replace("'",'"')
	data = json.loads(data)
	print("X: %s \t Y: %s \t Z: %s" %(data["X Axis"], data["Y Axis"], data["Z Axis"]))
	if ( abs(float(data["X Axis"].replace(',','.'))) > 2 or 
	     abs(float(data["Y Axis"].replace(',','.'))) > 2 or 
	     abs(float(data["Z Axis"].replace(',','.'))) > 2):
		print("----------------------------------------")
		ch.basic_publish(exchange = 'amq.topic', routing_key = 'android_rpc', body ='stop')
	
	
#Establish connection
credentials = pika.PlainCredentials('mqtt-test', 'mqtt-test')
connection=pika.BlockingConnection(pika.ConnectionParameters('localhost', 5672, '/', credentials))
channel=connection.channel()

#Declare the queues
channel.queue_declare(queue ='carSafety', durable = True)
channel.queue_declare(queue ='android_rpc')
channel.queue_declare(queue ='accel_data',  arguments={'x-message-ttl' : 100})

#Bind 'carSafety' queue to the amq.topic where the mqtt Car client publishes
channel.queue_bind(exchange = 'amq.topic', queue = 'carSafety')
channel.queue_bind(exchange = 'amq.topic', queue = 'accel_data')

#Sybscribe to updates from 
channel.basic_consume(carSafety_callback, queue = 'carSafety')



print("Waiting for messages...")

try:
	channel.start_consuming()
except:
	KeyboardInterrupt
	connection.close()
	sys.exit(0)
