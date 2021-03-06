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

## 使用方法

```shell
在 mider code 里, 称形如 >g>sequence 为一条轨道, 而形如 >!cmd> 为一条内建指令
# 轨道格式
>bpm[;mode][;pitch][;i=instrument][;timeSignature][;midi|img|pdf|mscz]>音名序列 | 唱名序列
bpm: 速度, 必选, 格式是: 数字 + b, 如 120b, 默认可以用 g(pitch=4&bpm=80) 或者 f(pitch=3&bpm=80) 代替
mode: 调式(若为小调则为同名小调), 可选, 格式是 b/#/-/+ 调式名, 如 Cminor, -Emaj, bC
pitch: 音域(音高), 可选, 默认为 4
i=instrument: 选择乐器, 可选
timeSignature: 拍号, 可选
midi: 是否仅上传 midi 文件, 可选
img: 是否仅上传 png 格式的乐谱
pdf: 是否仅上传 pdf 文件, 可选
mscz: 是否仅上传 mscz 文件, 可选
音名序列的判断标准是序列里是否出现了 c~a 或 C~B 中任何一个字符
# 获取帮助
>!help>
# 设置 formatMode
>!formatMode=mode>
# 清理缓存
>!clear-cache>
```
**注: 涉及乐谱生成需要先安装 Muse Score** 见[转换乐谱](#转换乐谱)

## 效果
![你看到了一张图](https://mirai.mamoe.net/assets/uploads/files/1653475903211-d7c4283d-8163-4079-8c60-11d26468d8b1-image.png)

## todo list

- [x] 解析音符为语音
- [x] 渲染乐谱
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
 % : 调整力度, 后接最多三位数字
 & : 还原符号
类似的用法还有 m-w, n-u, q-p, i-!, s-z 升高或降低度数在 ^-v 的基础上逐步递增或递减

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
10. (velocity linear from~to: note sequence) 调整 note sequence 的力度, 仅适用于长音名序列
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
同样支持多轨
```
>g>c d e (g 的 pitch 默认为 4
>f>a b c (f 的 pitch 默认为 3, 可以当作低音轨道
```

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

## 参考配置
```yaml
# ffmpeg 转换命令 (不使用 ffmpeg 也可以, 只要能完成 wav 到 mp3 的转换就行 , {{input}} 和 {{output}} 由 插件提供不需要修改
ffmpegConvertCommand: 'ffmpeg -i {{input}} -acodec libmp3lame -ab 256k {{output}}'
# timidity 转换命令 (不使用 timidity 也可以, 只要能完成 mid 到 wav 的转换就行
timidityConvertCommand: 'timidity {{input}} -Ow -o {{output}}'
# muse score 从 .mid 转换到 .mscz
mscoreConvertMidi2MSCZCommand: 'MuseScore3 {{input}} -o {{output}}'
# muse score 从 .mid 转换到 .pdf
mscoreConvertMSCZ2PDFCommand: 'MuseScore3 {{input}} -o {{output}}'
# muse score 从 .mid 转换到 .png 序列
mscoreConvertMSCZ2PNGSCommand: 'MuseScore3 {{input}} -o {{output}}'
# silk 比特率(吧
silkBitsRate: 24000
# 生成模式 可选的有:
# internal->java-lame (默认)
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
 - 唱名序列中 `\s{2}` 和 `\s\|\s` 会被自动替换成 `0` 也就是休止符, 可以在配置中修改这部分行为
 - 若为使用 `internal` 模式则生成的语音音色会随着系统底层实现的不同而不同
 - 如果最后输出的格式是 `silk` 那么好友和群聊都有效, 如果是 `mp3` 则仅群聊有效, 好友会出现感叹号
 - `mp3` 格式在 `pc` 端听不了, `mac` 据说可以 ~~, 哪位富婆可以给咱买一台测试一下(~~
 - 命令还未加入权限, 可以在 [#3](https://github.com/whiterasbk/MiraiMidiProduce/issues/3) 进行讨论
 - 好友环境下生成 `silk` 格式会比 `mp3` 音质低得多 ~~, 听个响属于是~~

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
