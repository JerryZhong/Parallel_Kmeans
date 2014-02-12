Parallel_Kmeans
===============

在etc/deploy.conf中记录了配置信息，包括kmeans并行化的hadoop输入存储路径hadoopinputdir，输出存储路径hadoopoutputdir。

关于parallel-kmeans的-files参数有如下几个：
1.clusterpoint:这个是初始化的时候随机生成的K个中心点，由init.py脚本负责生成
1.clusternumber:设置需要的聚类有几个中心点。
3.topicnumber:根据实际的主题个数定夺。因为每一个中心点有topicnumber个数量的主题。

resultdir:存放了每一次迭代的聚类中心点，每一次迭代生成一个文件，每一个文件最后一行有当前迭代的损失值


参数：
totaliteratetime：设置的最大总的迭代次数
thresholdvalue：设前一次迭代和这一次迭代的最大差值diff=|a-b|,如果小于这个差值，就跳出，终止迭代。


map阶段：
就是将每一个map分配到的点，查看离哪一个聚类点最近，然后分配个这个最近的聚类点。
每一个聚类点分配topic+2个内存空间。前面topic个sum各个维度之和，topic+1:计算这个聚类点有多少个点被聚集 topic+2:计算这个聚类点的损失值

reduce阶段：
统筹所有的partial-map的结果值。均值化结果值.

