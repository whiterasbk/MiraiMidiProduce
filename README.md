# MidiProduce

在线作曲插件

## 安装方法
下载见 `release`
1. 打开 `plugins` 文件夹
2. 丢进去
3. 关闭文件夹

## 使用方法

```shell
# 命令格式 (一个命令代表一条轨道)
>bpm[;mode][;pitch][;midi]>音名序列 | 唱名序列
bpm: 速度, 必选, 格式是: 数字 + b, 如 120b, 默认可以用 g 或者 f 代替
mode: 调式, 可选, 格式是 b/#/-/+ 调式名, 如 Cminor, -Emaj, bC
pitch: 音域(音高), 可选, 默认为 4
音名序列的判断标准是序列里是否出现了 c~a 或 C~B 中任何一个字符
midi: 是否仅上传 midi 文件
# 获取帮助
>!help>
```

## 效果
![你看到了一张图](https://mirai.mamoe.net/assets/uploads/files/1653475903211-d7c4283d-8163-4079-8c60-11d26468d8b1-image.png)

## todo list

- [x] 解析音符为语音
- [ ] 渲染乐谱
- [ ] 识别乐谱并转化为音符

## 关于音符序列

```
# 公用规则 (如无特殊说明均使用在唱名或音名后, 并可叠加使用)
 # : 升一个半音, 使用在音名或唱名前
 $ : 降一个半音, 使用在音名或唱名前
 + : 时值变为原来的两倍
 - : 时值变为原来的一半
 . : 时值变为原来的一点五倍
 : : 两个以上音符组成一个和弦
 ~ : 克隆上一个音符
 ^ : 克隆上一个音符, 并升高 1 度
 v : 克隆上一个音符, 并降低 1 度
 ↑ : 升高一个八度
 ↓ : 降低一个八度
 & : 还原符号
类似的用法还有 m-w, n-u, i-!, q-p, s-z 升高或降低度数在 ^-v 的基础上逐步递增或递减

# 如果是音名序列则以下规则生效
a~g: A4~G4
A~G: A5~G5
 O : 二分休止符 
 o : 四分休止符 
0-9: 手动修改音域

# 如果是唱名序列则以下规则生效
1~7: C4~B4
 0 : 四分休止符
 i : 升高一个八度
 ! : 降低一个八度
 b : 降低一个半音, 使用在唱名前
 * : 后接一个一位数字表示重复次数
 
# 宏
目前可用的宏有
1. (def symbol=note sequence) 定义一个音符序列
2. (def symbol:note sequence) 定义一个音符序列, 并在此处展开
3. (=symbol) 展开 symbol 对应音符序列
4. (include path) 读取 path 代表的资源并展开, 如果是文件默认目录是插件的数据文件夹
5. (repeat time: note sequence) 将音符序列重复 times 次
6. (ifdef symbol: note sequence) 如果定义了 symbol 则展开
7. (if!def symbol: note sequence) 如果未定义 symbol 则展开
8. (macro name param1[,params]: note sequence @[param1]) 定义宏
9. (!name arg1[,arg2]) 展开宏
目前宏均不可嵌套使用
```

## 示例

```
1. 小星星
>g>1155665  4433221  5544332  5544332
等同于
>g>ccggaag+ffeeddc+ggffeed+ggffeed
等同于
>g>c~g~^~v+f~v~v~v+(repeat 2:g~v~v~v+) (酌情使用

2. KFC 可达鸭
>g;bE>g^m+C-wmD+D^m+G-wmE+D^w+C-wmD+DvagaC

3. 碎月 
>85b>F+^$BC6GFG C$E F D$ED$b C+ g$b C$E F$E F+ F$E F$B G++ G$B C6C6$B C6 G+ G$E FGF$E C+ C$b C+C$EF$EFG $E
等同于
>85b;Cmin>F+^BC6GFG CE F DEDb C+ gb CE FE F+ FE FB G++ GB C6C6B C6 G+ GE FGFE C+ Cb C+CEFEFG E

4. 生日快乐
>88b>d.d- e+v g+ #f++ d.d- e+v a+ v+ d.d- D+b+g+ #f+ e+ C.C- b+ g+^ v+

5. 茉莉花
>110b>e+em^m~wv+g^v++e+em^m~wv+g^v++g+~~em^+av~++e+d^m+evv+c^v++evvmv+.eg+amg++d+egd^cwv++ ^-c+d+.ec^vwv++

6. bad apple!
>100b>e#fgab+ ED b+ e+ b a-- B-- A- g#f e#fga b+ ag #fe#fg #f--G--#F-e #d#f e#fgab+ ED b+e+ ba--B--A- g#f e#fgab+ ag

7. Jingle Bells
>100b>E~~+E~~+EmC^^++F~~+Fv~+Ev~^ D+G+E~~+E~~+EmC^^++F~~+Fv~~m~vDv++

8. 两只老虎 卡农
>g;3>(def tiger:1231 1231 3450 3450 5-6-5-4-31 5-6-5-4-31 15!10 15!10)
>g;4>00(=tiger)
>g;5>0000(=tiger)
>g;6>000000(=tiger)
>g;7>00000000(=tiger)
```
更多示例见 [awesome-melody](https://github.com/whiterasbk/MiraiMidiProduce/tree/master/awesome-melody)

若想分享自己编写的旋律欢迎提 `pr` 到这个文件夹, **建议使用英文名称**, 后续可能会考虑打包进发布版本供 `include` 使用

## 和弦

轨道内可以使用`:`构建 
```
>g>c:e:g 构建大三和弦
```
也可以写成下面的等价形式
```
>g>c:m:m (m 是将前一个音符克隆并升高两度 
```
同时使用`:`和 功能类似 `m`, `^`等的字符 则 `#` 和 `$` 将不起作用, 可以使用 `#` 和 `$` 的等价后缀修改符 `"` 和 `'`, ~~问就是起名废~~
```
>g>c:m:#m (# 号将不起作用
>g>c:m:m" (这种形式能正常解析
```
同样支持轨道多轨
```
>g>c d e (g 的 pitch 默认为 4
>f>a b c (f 的 pitch 默认为 3, 可以当作低音轨道
```

## 配置
```yaml
# ffmpeg 转换命令 (不使用 ffmpeg 也可以, 只要能完成 wav 到 mp3 的转换就行
ffmpegConvertCommand: 'ffmpeg -i {{input}} -acodec libmp3lame -ab 256k {{output}}'
# timidity 转换命令 (不使用 timidity 也可以, 只要能完成 mid 到 wav 的转换就行
timidityConvertCommand: 'timidity {{input}} -Ow -o {{output}}'
# silk 比特率(吧
silkBitsRate: 24000
# 格式转换输出 可选的有:
# internal->java-lame(默认)
# internal->java-lame->silk4j
# timidity->ffmpeg
# timidity->ffmpeg->silk4j
# timidity->java-lame
# timidity->java-lame->silk4j
formatMode: 'internal->java-lame'
# 宏是否启用严格模式
macroUseStrictMode: true
# 是否启用调试
debug: false
# 是否启用缓存
cache: true
# 是否启用空格替换
isBlankReplaceWith0: true
# 量化深度 理论上越大生成 mp3 的质量越好, java-lame 给出的值是 256
quality: 64
# 超过这个大小则自动改为文件上传
uploadSize: 1153433
```

## 注意
 - 唱名序列中 `\s{2}` 和 `\s\|\s` 会被自动替换成 `0` 也就是休止符
 - 如果最后输出的格式是 `silk` 那么好友和群聊都有效, 如果是 `mp3` 则仅群聊有效, 好友会出现感叹号
 - `pc` 端听不了, `mac` 据说可以~~哪位富婆可以给咱买一台测试一下(~~
 - 命令还未加入权限
 - 好友环境下生成 `silk` 格式会比 `mp3` 音质低得多, 听个响属于是

## 关于构建

`clone` 到本地后修改 `build.gradle.kts` 中的 `uesrname` 和 `password` 为自己的即可成功构建

## 关于 timidity 和 ffmpeg
在服务器环境插件可能会由于缺少硬件或驱动支持无法生成语音, 可以尝试安装 `timidity` 和 `ffmpeg` 解决

具体的安装可以参考 [这篇](https://www.cnblogs.com/koujiaonuhan/p/aliyun_centos65_install_ffmpeg_libmp3lame_timidity_to_convert_midi_to_mp3.html)

这里提供一个 `sf2` 的 [音色库](https://cowtransfer.com/s/2f42efd92be448)

安装完成以后确保 `timidity` 和 `ffmpeg` 位于环境变量中, 或者也可以修改 `ffmpegConvertCommand` 和 `timidityConvertCommand`

最后修改 `formatMode` 即可使用 `timidity` 和 `ffmpeg` 生成语音

## 关于修改音色

目前只能通过安装 `timidity` 来实现

## release 中的多个发行包

带 `bundled-silkf4` 的是打包了 [silk4j](https://github.com/mzdluo123/silk4j) 的包

若确定不需要使用转换 `silk` 的功能可以直接下载不带后缀版本的包
