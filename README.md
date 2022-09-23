# MidiProduce

![](https://img.shields.io/github/downloads/whiterasbk/MiraiMidiProduce/total) 
![](https://img.shields.io/github/v/release/whiterasbk/MiraiMidiProduce?display_name=tag)
![](https://img.shields.io/github/languages/top/whiterasbk/MiraiMidiProduce)
![GitHub](https://img.shields.io/github/license/whiterasbk/MiraiMIdiProduce)

在线作曲插件

## 安装方法
下载见 `release`
1. 打开 `plugins` 文件夹
2. 丢进去
3. 关闭文件夹

## 效果
![你看到了一张图](https://mirai.mamoe.net/assets/uploads/files/1653475903211-d7c4283d-8163-4079-8c60-11d26468d8b1-image.png)
[BV1gS4y1W7cj](https://www.bilibili.com/video/BV1gS4y1W7cj)

## 使用方法

### 普通指令(轨道语法): `>params>sequence`
在 mider code 中, `>params>sequence` 可以称作一条轨道
#### params
参数之间用`;`号分隔
支持的参数 `params` 详细说明如下:

1. `g|f|数字+b` 设置 bpm 同时确定音域, f 表示低音 (pitch=3&bpm=80), g 表示高音 (pitch=4&bpm=80) **必选参数不可省略**. 示例:
```shell
>g>
>f>
>120b>
```
**所有轨道共用一个 bpm**
2. `[数字+x]` 设置倍速, 支持整数和小数
3. `[/+数字]` 以设定值为音符默认时值
4. `[数字+%]` 设置轨道音量
5. `[数字+dB]` 以设定值为音符默认音量, 范围 0~127
6. `[调号]` 默认当前音符序列为 C大调 并将其转调到指定调号. 调号由两部分组成: 一部分是调的名字, 具体参考乐理, 需要大写, 另一部分是调的模式, major 和 minor 分别可以简写成 maj 和 min. 若为 minor 则为同名小调. 在一般情况下 major 可以省略. 示例:
```text
>g;Cmin>
>g;#Fmajor>
>g;bD>
```
7. `[数字]` 设置默认音高
8. `[数字/数字]` 设置拍号
9. `[i=instrument]` 设置乐器, `instrument` 见 [MidiInstrument.kt](https://github.com/whiterasbk/mider/blob/dev/src/main/kotlin/whiter/music/mider/MidiInstrument.kt)
10. `[midi|img|pdf|mscz]` 上传乐谱文件, 如是 img 则会上传图片 **注: 涉及乐谱生成需要先安装 Muse Score** 见 [转换乐谱](#转换乐谱)
11. `[sing:area:singerId]` 调用 [sinsy](https://www.sinsy.jp/) 接口生成音频. 现在只支持 单个音符 和 休止符. 当歌词包含英文时必须使用音名序列. 当 area 和 singerId 都提供时, singerId的格式为 [f|m]+数字 表示选取的是该地区的 第 singerId 位 女性|男性 歌手. 当仅提供 singerId 时, 其格式为 数字, 表示选取 表示符 为 singerId 的歌手. 当两者都不提供时, 相当于选取中国大陆地区的第一位女歌手: 香玲. area 的取值目前只有 cn|us|jp

#### sequence
sequence 可以是音名序列或是唱名序列, 音名序列的判断标准是序列里是否出现了 `c~a` 或 `C~B` 中任何一个字符
##### 音符
音名序列下, 使用 cdefgabCDEFGAB 表示, 其中小写写表示 pitch=4的音符, 大写表示比小写高一个八度的音符 

唱名序列下, 使用 1234567 表示

创建的音符默认时值是四分音符, 要修改其时值, 可以使用 `+` 来拉长音符时值, `-` 以缩短时值
例如以下示例就创建了一个八分音符, 十六分音符和一个二分音符
```shell
>g>c- #八分音符
>g>c-- #十六分音符
>g>c+ #二分音符
```
要为音符添加附点, 请在该音符后加上 `.`, 而特殊时值例如三连音可以使用 `/`
```shell
>g>a. c/3 c/3 c/3
```
`#` 和 `$` 分别可以让音符升高或降低半音, `@` 表示给音符添加一个还原符号
```text
>g>$e @f #c
```
`&` 可以将多个音符的时值连在一起组成一个音符
```shell
>g>a&a- # 等价于以下
>g>a.
```
而在唱名序列中, `$` 可以用 `b` 代替
```shell
>g>12b3
```
在音符后加上数字可以改变其八度
```shell
>g>c3 e5
```
而更推荐的做法是使用 `↑` 和 `↓` 在 pitch=4 的基础上进行八度的增减. 而在唱名序列中, `↑` 和 `↓` 可以用 `i` 和 `!` 代替
```shell
>g>c↓ e↑ g↑↑
>g>1↓ 3↑ 5↑↑
>g>1! 3i 5ii
```
`%` 可以设置单个音符的力度, 范围是 0~127, 未设置的情况下默认力度为 100
```shell
>g>c%60
```
使用 `o` 或 `O` 创建四分休止符或二分休止符, 休止符同样可以使用 `+`, `-` `.` 甚至 `/` 来修改时值. 在 唱名序列中 `0` 相当于 四分休止符
```shell
>g>o-O+O.
```
`[` 配合 `]` 可以给单个音符加上歌词
```shell
>g>1[打]2[倒]3[列]1[强]
```
要重复单个音符多次可以使用 `*`
```shell
>g>c*100
```
`~` 可以克隆前一个音符, ~~适合偷懒~~
```shell
>g>c~~~~~~
```
`^` 和 `v` 可以将上一个音符克隆并升高或降低一个音, 升高或降低的音满足在 C大调 下的音程关系. 类似的用法还有 `m-w`, `n-u`, `q-p`, `i-!`, `s-z` 升高或降低度数在 ^-v 的基础上逐步递增或递减
```shell
>g>c^^^ # 等价于 cdef
```

##### 和弦
使用 `:` 可以将多个音符组成一个和弦, 第一个音的时值将会是和弦的时值
```shell
>g>c:e:g
```
`^` 和 `v` 等 也是可用的
```shell
>g>c:m:m # 等价于 c:e:g
```
但是 使用 `^` 和 `v` 等 时要注意, `#` 和 `$(b)` 将不起作用
```shell
>g>c:m:#m  #号 将不起作用
```
可以使用 `"` 和 `'` 代替 `#` 和 `$(b)` ~~问就是起名废~~
```shell
>g>c:m:m'
```
`↟` 和 `↡` 可以创建向上或向下琶音 ~~符号越来越奇怪了啊喂~~
```shell
>g>c:e:g↟
```
`t` 可以使 和弦中的音符的时值可以独立作用, 此时和弦的时值是组成音符中时值最长的那个
```shell
>g>c+:e:g-t
```

##### 倚音
`;` 连接两个音符组成一个短前倚音, 倚音时值为第二个音符的时值
```shell
>g>c;e
```
若要构建后倚音只需要在第二个音符后加上 `t`
```shell
>g>c;et
```

##### 滑音/刮奏(Glissando)
使用 `=`, 可以连接多个音符, 时值为所有组成音符的时值. 默认只刮白键
```shell
>g>c=b
```
若要白键和黑键一起刮, 在后面加上 `t`
```shell
>g>c=bt
```

##### 宏
mider code 中宏的本质是对某段序列或其中的字母或数字的重复或简单修改替换. 

碍于技术原因, 目前宏均不可嵌套使用

宏的定义始于 `(` 终于 `)`, `()` 内便是宏的作用域, 以下是支持的宏: 

定义一个音符序列: `(def symbol=note sequence)`
```shell
>g>aaa(def na=cde)aaa
>g>aaaaaa # 实际输入的序列
```
定义一个音符序列, 并在此处展开: `(def symbol:note sequence)` 
```shell
>g>aaa(def na=cde)aaa
>g>aaacdeaaa # 实际输入的序列
```
展开 symbol 对应音符序列: `(=symbol)`
```shell
>g>(def a=cde)a(=a)
>g>acde # 实际输入的序列
```
读取 path 代表的资源并展开, 如果是文件默认目录是插件的数据文件夹: `(include path)` 
```shell
>g>(include ./seq.midercode)
```
将音符序列重复 times 次: `(repeat time: note sequence)`
```shell
>g>c(repeat 3: oa)
>g>coaoaoa # 实际输入的序列
```
如果定义了 symbol 则展开: `(ifdef symbol: note sequence)`

如果未定义 symbol 则展开: `(if!def symbol: note sequence)`
```shell
>g>(def s=abc) (ifdef s: cfg) (if!def s: bbc) 
>g>cfg #实际输入的序列
```
定义宏(类似函数, 但实际表现得更蠢一些): `(macro name param1[,params]: note sequence @[param1])`

展开宏: (!name arg1[,arg2])
```shell
>g>(m p1: a@[p1]dc@[p1]) (!m b)
>g>abdcb #实际输入的序列
```
调整 note sequence 的力度, 仅适用于长音名序列: `(velocity linear from~to: note sequence)`
```shell
>g>(velocity linear 50~80: cde)
>g>c%50 d%60 e%70
```

### 环境指令: `>!config>`
供 MidiProduce 内部调用
获取帮助
```shell
>!help>
```
设置 formatMode
```shell
>!formatMode=mode>
```
清理缓存
```shell
>!clear-cache>
```

## todo list

- [x] 解析音符为语音
- [x] 渲染乐谱
- [ ] 识别乐谱并转化为音符


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

## 转换乐谱
**此功能需要首先安装 [Muse Score](https://musescore.org/zh-hans)**

下载 `Muse Score` : 可以根据官方 [下载页面](https://musescore.org/zh-hans/download) 也可以参考 [snapcraft](https://snapcraft.io/musescore)

附官方 `linux` [安装指北](https://musescore.org/zh-hans/handbook/3/install-linux) 

安装完成后将 `Muse Score` 的运行目录 (包括`bin/`) 添加到环境变量

或者也可以修改配置中`mscoreConvertMidi2MSCZCommand` 等的值为安装目录

#### 如您的安装的可执行程序启动命令(可执行程序的名字)不是 `MuseScore3`, 您需要手动将 `config.yml` 中的 `MuseScore3` **替换**成正确的 `MuseScore` 启动命令

最后在轨道中添加 `;pdf` 或 `;img` 即可得到渲染好的乐谱

![44f9b717-4c28-453e-b99c-2fc8567828c8-image.png](https://mirai.mamoe.net/assets/uploads/files/1654083503837-44f9b717-4c28-453e-b99c-2fc8567828c8-image.png)

若想修改 `Muse Score` 命令格式和参数, 请参考 [官方使用手册](https://musescore.org/zh-hans/handbook)

## 注意
 - 唱名序列中 `\s{2}` 和 `\s\|\s` 会被自动替换成 `0` 也就是休止符, 可以在配置中修改这部分行为
 - 若为使用 `internal` 模式则生成的语音音色会随着系统底层实现的不同而不同
 - 如果最后输出的格式是 `silk` 那么好友和群聊都有效, 如果是 `mp3` 则仅群聊有效, 好友会出现感叹号
 - `mp3` 格式在 `pc` 端听不了, `mac` 据说可以 ~~, 哪位富婆可以给咱买一台测试一下(~~
 - 命令还未加入权限, 可以在 [#3](https://github.com/whiterasbk/MiraiMidiProduce/issues/3) 进行讨论
 - 好友环境下生成 `silk` 格式会比 `mp3` 音质低得多 ~~, 听个响属于是~~
 - 当文本过于长超过 QQ 消息的限制时, 可以将 midercode 保存到文本文件中并修改其扩展名为 .midercode, 上传后 机器人同样能正常识别

## 构建

由于使用了 [RainChan](https://github.com/mzdluo123) 的 [silk4j](https://github.com/mzdluo123/silk4j) 所以 `clone` 到本地后要修改 `build.gradle.kts` 中的 `username` 和 `password` 为自己的才能成功构建

## 服务器环境下生成语音

在服务器环境插件可能会由于缺少硬件或驱动支持无法生成语音, 可以尝试安装 [timidity](http://timidity.sourceforge.net/) 和 [ffmpeg](http://ffmpeg.org/) 解决

具体安装可以参考 [这篇](https://www.cnblogs.com/koujiaonuhan/p/aliyun_centos65_install_ffmpeg_libmp3lame_timidity_to_convert_midi_to_mp3.html)

这里提供一个 `sf2` 的 [音色库](https://cowtransfer.com/s/2f42efd92be448)

安装完成以后确保 `timidity` 和 `ffmpeg` 位于环境变量中, 或者也可以修改 `ffmpegConvertCommand` 和 `timidityConvertCommand`

最后修改 `formatMode` 即可使用 `timidity` 和 `ffmpeg` 生成语音

## 修改音色

目前 `internal` 无法修改音色

可以通过安装 `timidity` 或 `Muse Score` 来实现


## release 中的多个发行包

带 `bundled-silkf4` 的是打包了 [silk4j](https://github.com/mzdluo123/silk4j) 的包

若确定不需要使用转换 `silk` 的功能可以直接下载不带后缀版本的包
