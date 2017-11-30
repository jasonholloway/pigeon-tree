namespace java com.woodpigeon.sapling.schema

struct Pos {
    double x
    double y
}

struct Vector {
    double x
    double y
    double angle
}

struct ComputedBranch {
    Pos start
    Pos end
}

struct Branch {
    double angle
    list<Branch> children
    ComputedBranch computed
}

struct Tree {
    Branch start
}
