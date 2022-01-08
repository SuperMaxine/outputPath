# outputPath

## 2021年12月28日
- [x] 基本完成路径输出
- [x] 添加打印输出方法
- [x] 添加对lookaround中Pos和Neg的支持

## 2021年12月29日
- [x] 添加对lookaround中Behind和NotBehind的支持
- [x] 添加三段分割方法(记录形如`(|)`、`()`的节点所生成的串的起止位置)

## 2022年1月3日-2022年1月4日
- [x] 分支结构的Path
- [x] 新结构的打印输出

之后要记录更新的详细记录：

## 2022年1月8日
- [x] 获取**每一个长度大于二的counting**的前缀和中缀
  - 在Analyzer类中新建成员变量
    - Map<Path, Path> countingBiggerThan2，以中缀的结束节点作为Key，开始节点作为Value
    - Map<Path, ArrayList<Set<Integer>>> PrePaths，以中缀的开始节点作为Key
    - Map<Path, ArrayList<Set<Integer>>> PumpPaths，以中缀的结束节点作为Key
  - 在Analyzer类中新建函数
    - generateAttackArgs()，递归遍历Path树，生成PrePath和PumpPath放入PrePaths和PumpPaths中
