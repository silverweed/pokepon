#!/usr/bin/python
# Perform a stress test for the pokepon server simulating
# several connected clients talking, disconnecting and connecting.
# Author: silverweed
import telnetlib
from sys import stderr
from sys import argv
import time
import random
import string
from threading import Thread

connected_clients = []

class Client(Thread):

	th_id_pool = 0
	MAX_SLEEP_TIME = 30
	MAX_SENTENCE_LEN = 255
	# (command, #args)
	commands = [	('help', 0),
			#('nick', 1),
			#('register', 2),
			('list', 0),
			('users', 0),
			('whois', 1), 
			('whoami', 0),
			('pm', 2),
			('serverinfo', 0),
			#('disconnect', 0)
	]
	CHARS = string.ascii_letters + string.digits + (' ' * 20)
	try:
		WORDS = open('/usr/share/dict/words').read().splitlines()
	except:
		WORDS = None
		stderr.write("[info] /usr/share/dict/words not found.\n")

	def __init__(self, serverip, serverport):
		Thread.__init__(self)
		self.serverip = serverip
		self.serverport = serverport
		self.th_id = Client.th_id_pool
		Client.th_id_pool += 1
		stderr.write("Created thread #"+str(self.th_id)+"\n")

	def run(self):
		if self.connect():
			# change nick to bot_<th_id>
			time.sleep(3)
			self.connection.write('/nick '+self.name()+"\n")
			# register to connected_clients
			global connected_clients
			connected_clients.append(self)
			# enter routine loop
			while self.routine():
				try:
					self.connection.read_eager()	# empty the incoming buffer
				except EOFError:
					stderr.write("Thread #"+str(self.th_id)+": no data available.\n")
					return
				rand_t = random.random() * Client.MAX_SLEEP_TIME
				time.sleep(rand_t)

	def name(self):
		return 'bot_'+str(self.th_id)

	def connect(self):
		try:
			stderr.write("Thread #"+str(self.th_id)+": connecting to "+self.serverip+":"+self.serverport+"...\n")
			self.connection = telnetlib.Telnet(self.serverip, self.serverport)
			stderr.write("Thread #"+str(self.th_id)+": new connection established.\n")
		except Exception as e:
			stderr.write("There was an exception in thread #"+str(self.th_id)+": "+str(e)+"\n")
			return False
		return True
	
	def routine(self):
		r = random.random()
		if r > 0.99:
			self.disconnect()
			return False
		elif r > 0.85:
			return self.issue_command()
		else:
			return self.talk()
	
	def disconnect(self):
		if self.connection != None:
			self.connection.close()
			global connected_clients
			connected_clients.remove(self)
			stderr.write("Thread #"+str(self.th_id)+": connection closed.\n")
			print 'Currently connected clients: ' + str([c.name() for c in connected_clients]) + "("+str(len(connected_clients))+")"
	
	def gen_sentence(self, max_len = MAX_SENTENCE_LEN):
		sentence_len = random.randint(1, max_len) / 10
		if Client.WORDS != None:
			sentence = ' '.join(random.choice(Client.WORDS) for n in range(1,sentence_len))
		else:	
			sentence = ''.join(random.choice(Client.CHARS) for n in range(1,sentence_len*5))
		return sentence

	def talk(self):
		sentence = self.gen_sentence()
		# write to the socket
		try:
			self.connection.write(sentence+"\n")
			return True
		except Exception as e:
			stderr.write("Thread #"+str(self.th_id)+": error writing to the connection: "+str(e))
			return False
	
	def issue_command(self):
		chosencmd = random.choice(Client.commands)
		cmd = '/' + chosencmd[0]
		if chosencmd[0] in ['whois', 'pm']:
			# 1st argsument must be a valid name
			global connected_clients
			user = random.choice(connected_clients)
			cmd += " " + user.name() 
			if chosencmd[0] == 'pm':
				cmd += " " + self.gen_sentence()
		else:
			for i in range(0,chosencmd[1]):
				cmd += " " + self.gen_sentence(20)

		try:
			self.connection.write(cmd+"\n")
			return True
		except Exception as e:
			stderr.write("Thread #"+str(self.th_id)+": error writing to the connection: "+str(e))
			return False


##################### MAIN #####################
serverip = '127.0.0.1'
serverport = '12344'
initial_clients = 100
tot_clients = 0

if len(argv) > 1:
	serverip = argv[1]
	if len(argv) > 2:
		serverport = argv[2]
		if len(argv) > 3:
			initial_clients = int(argv[3])

print '#### Started stress test at ' + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()) + ' ####'
print '@Server IP: ' + serverip + ':' + serverport

for i in range(0, initial_clients):
	newclient = Client(serverip, serverport)
	newclient.start()
	time.sleep(0.01)
	tot_clients += 1

while True:
	if random.random() < 0.5:
		newclient = Client(serverip, serverport)
		newclient.start()
		tot_clients += 1
		print 'Added new client.'
		print 'Currently connected clients: ' + str([c.name() for c in connected_clients]) + "("+str(len(connected_clients))+")"
		print 'Total so far: ' + str(tot_clients)
	time.sleep(random.random() * 10)
