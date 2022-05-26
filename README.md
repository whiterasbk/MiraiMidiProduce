# MidiProduce

在线作曲插件

## 安装方法
下载见 `release`
1. 打开 `plugins` 文件夹
2. 丢进去
3. 关闭文件夹

## 使用方法

```shell
命令一般格式 
>bpm[;mode][;pitch]>音名序列|唱名序列
bpm: 速度, 必选, 格式是: 数字+b, 如 120b, 默认可以用 g 代替
mode: 调式, 可选, 格式是(b/#)调式名, 如Cminor, -Emaj
pitch: 音域(音高), 可选, 默认为 4

获取帮助
>!help>
```

## 关于音符序列

```
如果是音名序列则以下规则生效
a~g: A4~G4
A~G: A5~G5
0-9: 手动修改音域
# : 升一个半音
$ : 降一个半音
+ : 时值变为原来的两倍
- : 时值变为原来的一半
. : 时值变为原来的一点五倍
: : 两个以上音符组成一个和弦 (目前有bug, 尽量不要使用)
~ : 克隆上一个音符
^ : 克隆上一个音符, 并升高1度
v : 克隆上一个音符, 并降低1度

+-.这三个符号对简谱序列也生效, 简谱序列暂未支持b和#
类似的用法还有m-w, n-u, i-!, q-p, s-z,升高或降低度数在^-v的基础上逐步递增或递减
```

## 示例
1. KFC 可达鸭
```shell
>g;-E>g^m+C-wmD+D^m+G-wmE+D^w+C-wmD+DvagaC
```

2. 生日快乐
```shell
>88b>d.d- e+v g+ f#++ d.d- e+v a+ v+ d.d- D+b+g+ f#+ e+ C.C- b+ g+^ v+
```

3. 碎月
```shell
>85b>F+^B$C6GFG CE$ F DE$Db$ C+ gb$ CE$ FE$ F+ FE$ FB$ G+ + GB$ C6C6B$ C6 G+ GE$ FGFE$ C+ Cb$ C+CE$FE$FG E$
```

4. 茉莉花
```shell
>110b>e+em^m~wv+g^v++e+em^m~wv+g^v++g+~~em^+av~++e+d^m+evv+c^v++evvmv+.eg+amg++d+egd^cwv++ ^-c+d+.ec^vwv++
```

5. bad apple!
```shell
>100b>ef#gab+ ED b+ e+ b a-- B-- A- gf# ef#ga b+ ag f#ef#g f#--G--F#-e d#f# ef#gab+ ED b+e+ ba--B--A- gf# ef#gab+ ag
```

6. Jingle Bells
```shell
>100b>E~~+E~~+EmC^^++F~~+Fv~+Ev~^ D+G+E~~+E~~+EmC^^++F~~+Fv~~m~vDv++
```

7. 小星星
```shell
>g>1155665  4433221  5544332  5544332
```

## 配置
```yaml
quality: 64 # 生成的音频的质量
uploadSize: 1153433 # 音频超过这个大小时自动改为文件上传
```

## 注意
 - 当序列满足 `[0-9.\s-+*/|]+` 时会自动判定为唱名序列, 否则为音名序列
 - 简谱序列中 `\s{2}` 和 `\s\|\s` 会被自动替换成 `0` 也就是休止符
 - 仅群聊有效
 - 电脑端听不了
 - 命令还未加入权限