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
>bpm[;mode]>Notes
获取帮助
>!help> 
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

## 配置
```yaml
quality: 64 # 生成的音频的质量
uploadSize: 1153433 # 音频超过这个大小时自动改为文件上传
```

## 注意
 - 仅群聊有效
 - 电脑端听不了
 - 命令还未加入权限