from pymongo import Connection
import sys,io,json,re
from bson import BSON, json_util
connection = Connection('localhost', 27017)
collection = connection['pingpong']['preAlpha']
for c in collection.find({"type":"frilp_user"}):
	if (len(sys.argv) == 2 and sys.argv[1] == "id"):
		fname = c['id']+u'.json'
	else:
		name=collection.find_one({"type":"fbprofile", "id":c['id']})['name'].replace(' ','')
		fname = name+u'.json'
	with open(fname,"w") as outfile:
		bsondata = str(collection.find_one({"type":"friends", "id":c['id']}))
		jsondata = re.sub(r'ObjectId\s*\(\s*\"(\S+)\"\s*\)', r'{"$oid": "\1"}', bsondata)
		outfile.write(json_util.dumps(jsondata))
	outfile.close()
