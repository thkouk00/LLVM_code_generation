@.example_vtable = global [0 x i8*] []
@.Adder_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32*, i32)* @Adder.add to i8*)]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
	%len = alloca i32
	%arr = alloca i32*
	%ad = alloca i8*
	store i32 10, i32* %len
	%_0 = load i32, i32* %len
	%_3 = icmp slt i32 %_0, 0
	br i1 %_3, label %oob5, label %oob6
	oob5:
	call void @throw_oob()
	br label %oob6
	oob6:
		%_4 = add i32 %_0, 1
	%_1 = call i8* @calloc(i32 4, i32 %_4)
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2
	store i32* %_2, i32** %arr
	%_8 = load i32*, i32** %arr
	%_7 = load i32 , i32* %_8
	%_9 = icmp ult i32 3, %_7
	br i1 %_9, label %oob10, label %oob11
	oob10:
	%_13 = add i32 3, 1
	%_14 = getelementptr i32, i32* %_8, i32 %_13
	store i32 20, i32* %_14
	 br label %oob12
	oob11:
	call void @throw_oob()
	 br label %oob12
	oob12:
	%_16 = load i32*, i32** %arr
	%_15 = load i32 , i32* %_16
	%_17 = icmp ult i32 6, %_15
	br i1 %_17, label %oob18, label %oob19
	oob18:
	%_21 = add i32 6, 1
	%_22 = getelementptr i32, i32* %_16, i32 %_21
	store i32 9, i32* %_22
	 br label %oob20
	oob19:
	call void @throw_oob()
	 br label %oob20
	oob20:
	%_24 = load i32*, i32** %arr
	%_23 = load i32 , i32* %_24
	%_25 = icmp ult i32 9, %_23
	br i1 %_25, label %oob26, label %oob27
	oob26:
	%_29 = add i32 9, 1
	%_30 = getelementptr i32, i32* %_24, i32 %_29
	store i32 5, i32* %_30
	 br label %oob28
	oob27:
	call void @throw_oob()
	 br label %oob28
	oob28:
	%_31 = call i8* @calloc(i32 1, i32 8)
	%_32 = bitcast i8* %_31 to i8***
	%_33 = getelementptr [1 x i8*], [1 x i8*]* @.Adder_vtable, i32 0, i32 0
	store i8** %_33, i8*** %_32
	store i8* %_31, i8** %ad
	%_34 = load i8*, i8** %ad
	; Adder.add : 0
	%_35 = bitcast i8* %_34 to i8***
	%_36 = load i8**, i8*** %_35
	%_37 = getelementptr i8*, i8** %_36, i32 0
	%_38 = load i8*, i8** %_37
	%_39 = bitcast i8* %_38 to i32 (i8*, i32*, i32)*
	%_41 = load i32*, i32** %arr
	%_42 = load i32, i32* %len
	%_40 = call i32 %_39(i8* %_34, i32* %_41, i32 %_42)
	call void (i32) @print_int(i32 %_40)


	ret i32 0
}

define i32 @Adder.add(i8* %this, i32* %.arr, i32 %.len) {
	%arr = alloca i32*
	store i32* %.arr, i32** %arr
	%len = alloca i32
	store i32 %.len, i32* %len
	%result = alloca i32
	%i = alloca i32
	%temp = alloca i32
	store i32 5, i32* %result
	store i32 0, i32* %i
	%_1 = load i32*, i32** %arr
	%_0 = load i32 , i32* %_1
	%_2 = icmp ult i32 3, %_0
	br i1 %_2, label %oob3, label %oob4
	oob3:
	%_6 = add i32 3, 1
	%_7 = getelementptr i32, i32* %_1, i32 %_6
	store i32 3, i32* %_7
	 br label %oob5
	oob4:
	call void @throw_oob()
	 br label %oob5
	oob5:
	%_8 = load i32, i32* %result
	ret i32 %_8
}

