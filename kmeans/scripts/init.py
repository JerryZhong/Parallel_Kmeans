#/usr/bin/python
#!coding=utf-8
import sys
import math
import random


def main(clusternumber,topicnumber,filename):
	fout=open(filename,"w")
	for i in range(int(clusternumber)):
		templist=[]
		sum=0.0
		tempstr=str(i)+"\t"
		for i in range(int(topicnumber)):
			temp=random.random()
			templist.append(temp)
			sum=sum+temp
		for i in range(int(topicnumber)):
			templist[i]=(templist[i]*1.0)/sum
		tempstr=tempstr+" ".join( (str(x)).strip() for x in templist[0:len(templist)] )+"\n"
		fout.write(tempstr)
	fout.write("loss\t999999\n")
	fout.close()

if __name__=="__main__":
	clusternumber=sys.argv[1]
	topicnumber=sys.argv[2]
	filename=sys.argv[3]
	main(clusternumber,topicnumber,filename)
