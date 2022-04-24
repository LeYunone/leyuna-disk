# 工程简介
乐云一私用云盘，自主研发中


# 记录思路

### 使用云盘的一个流程，
1、首先问服务器我有资格上传文件吗[ip？文件名？]

2、得到答复后，准备上传文件，服务器问我，这些文件你需要放在哪里

3、我给予答复，我这边按照你给的文件规则上传文件，服务器将文件进行归类整理。[时间、名称、大小...的权重]

4、服务器将文件进行缓存处理，并且问我这些文件需要存储多久。

4、文件存储永久，则进入磁盘中。 否则将存储时间进行一个标记和消息推送，等我打开文件管理后台进行处理。


## 重点[文件我到底要怎样处理]

### 难点
1、 怎么判断这些文件是不是要真正进行存储，比如如果是一张图片？或是一个文本，是不是可以进行代码处理，把文件变成数据流或是代码

2、 还是存储问题，是否可以将推送过来的文件进行类似压缩操作，将文件大小缩小

3、分片上传、断点续传

4、并发场景解决

5、大文件阻塞单个线程问题

6、数据的完整性

7、断点续传，前后端续定好一致规则
