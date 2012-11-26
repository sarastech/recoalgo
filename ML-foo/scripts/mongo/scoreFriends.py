from pymongo import Connection
import sys,io,json,re,operator
from bson import BSON, json_util
from collections import Counter

connection = Connection('localhost', 27017)
collection = connection['pingpong']['preAlpha']
scoreList = []

def getCheckinList(_fbid):
	scoreList=[]
	checkins = collection.find_one({"type":"checkins", "id":_fbid})
	if(checkins):
		for checkin in checkins['data']:
			scoreList.append(checkin['from']['id'])
			if ('tags' in checkin.keys()):
				for tag in checkin['tags']['data']:
					scoreList+=[tag['id']]
	return scoreList

def getName(_fbid):
	fbprofile = collection.find_one({"type":"fbprofile", "id":_fbid})
	if (fbprofile):
		return fbprofile['name']
	return "None"

def inwardScore(_fbid):
	scoreList=[]
	statuses = collection.find_one({"type":"statuses", "id":_fbid})
	if (statuses):
		for status in statuses['data']:
			if ('likes' in status.keys()):
				for like in status['likes']['data']:
					scoreList+=[like['id']]
					
			if ('comments' in status.keys()):
				for comment in status['comments']['data']:
					scoreList+=[comment['id']]
	return scoreList		
def outwardScore(_fbid):
	scoreList=[]
	friends = collection.find_one({"type":"friends", "id":_fbid})
	for friend in friends['data']:
		#print friend['id']
		statuses = collection.find_one({"type":"statuses", "id":friend['id']})
		if (statuses):
			for status in statuses['data']:
				if ('likes' in status.keys()):
					for like in status['likes']['data']:
						if like['id'] == _fbid:
							scoreList+=[friend['id']]
				if ('comments' in status.keys()):
					for comment in status['comments']['data']:
						if comment['id'] == _fbid:
							scoreList+=[friend['id']]
	return scoreList

def sharedWorkHistory(_fbid):
	workPlaces = []
	workHistory = []
	profile = collection.find_one({"type":"fbprofile","id":_fbid})
	if 'work' in profile.keys():
		for w in profile['work']:
			if (w['employer']['id']	and w['employer']['id'] != "None"):
				workPlaces+=[w['employer']['id']]
	friends = collection.find_one({"type":"friends", "id":_fbid})
	for friend in friends['data']:
		profile = collection.find_one({"type":"fbprofile","id":friend['id']})
		if profile and 'work' in profile.keys():
			for w in profile['work']:
				if (w['employer']['id'] in workPlaces):
					workHistory+=[friend['id']]
					#print getName(friend['id'])
					break
	return workHistory

def sharedEducationHistory(_fbid):
        schools = []
        schoolHistory = []
        profile = collection.find_one({"type":"fbprofile","id":_fbid})
	if 'education' in profile.keys():
		for e in profile['education']:
			if (e['school']['id'] and e['school']['id'] !=None):
				schools+=[e['school']['id']]
	friends = collection.find_one({"type":"friends", "id":_fbid})
	for friend in friends['data']:
		profile = collection.find_one({"type":"fbprofile","id":friend['id']})
		if profile and 'education' in profile.keys():
			for e in profile['education']:
				if (e['school']['id'] in schools):
					schoolHistory+=[friend['id']]
					#print getName(friend['id'])
					break
	return schoolHistory

def countScores(list):
	returnList=[]
	for (uid, count) in Counter(list).most_common(20):
		returnList+=[uid]
	return returnList



fbid = "657673551" 
#fbid = "508786423" 
#fbid = "100004133943578"
#fbid = "509544910"
for c in collection.find({"type":"frilp_user","id":fbid}):
	inwardScoreList = countScores(inwardScore(fbid))
	outwardScoreList = countScores(outwardScore(fbid))
	checkinList = countScores(getCheckinList(fbid))
	workHistoryList = sharedWorkHistory(fbid)
	educationHistoryList = sharedEducationHistory(fbid)
	#for id in  educationHistoryList:
	#	print getName(id)
