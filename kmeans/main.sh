#!/bin/sh

. etc/deploy.conf

###记录迭代次数，计数用
iteratetime=0

###记录前一次迭代的损失值
preloss=0

###记录当前迭代的损失值
currentloss=0

rm -fr $resultdir/*
#rm -fr $clusterpoint
while [ $iteratetime -lt $totaliteratetime ]
do
	if [ $iteratetime  -eq  0 ]
	then
		python ${scripts}/init.py $clusternumber $topicnumber $clusterpoint   ###initiate data
        preloss=99999
	fi
	$hadoopbin fs -rmr $hadoopoutputdir;$hadoopbin jar kmeans.jar -files $clusterpoint  $topicnumber  $clusternumber  $hadoopinputdir $hadoopoutputdir
    mv $clusterpoint   ${resultdir}/iter_${iteratetime}

	$hadoopbin fs -get $hadoopoutputdir/part-r-00000  $clusterpoint

    currentloss=`tail -n 1 ${clusterpoint} | awk -F["\t"] '{print $2}'`
    echo "previous loss:"${preloss},"current loss:",$currentloss
    diff=`echo "scale=6;a=$preloss-$currentloss;if ((length(a)==scale(a)) &&(a!="0") ) print 0;print a"|bc`
    a=`expr $thresholdvalue \> $diff`
    if [ $a -gt 0 ]
	then
		break
    else
        preloss=$currentloss
	fi
	iteratetime=`expr $iteratetime + 1 `
    echo "iteratetime:",$iteratetime,`date`
done
