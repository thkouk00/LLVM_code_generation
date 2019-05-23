@.ArrayTest_vtable = global [0 x i8*] []
@.Test_vtable = global [1 x i8*] [i8* bitcast (i1 (i8*, i32)* @Test.start to i8*)]

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
	%n = alloca i1
	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	; Test.start : 0
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i1 (i8*, i32)*
	%_8 = call i1 %_7(i8* %_0, i32 10)
	store i1 %_8, i1* %n


	ret i32 0
}

define i1 @Test.start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%b = alloca i32*
	%l = alloca i32
	%i = alloca i32
	%_0 = load i32, i32* %sz
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
	store i32* %_2, i32** %b
	%_8 = load i32*, i32** %b
	%_7 = load i32, i32* %_8
	store i32 %_7, i32* %l
	store i32 0, i32* %i
	br label %loop9
	loop9:
	%_12 = load i32, i32* %i
	%_13 = load i32, i32* %l
	%_14 = icmp slt i32 %_12, %_13
	br i1 %_14, label %loop10, label %loop11

	loop10:
	%_16 = load i32*, i32** %b
	%_15 = load i32 , i32* %_16
	%_17 = load i32, i32* %i
	%_18 = icmp ult i32 %_17, %_15
	br i1 %_18, label %oob19, label %oob20
	oob19:
	%_22 = add i32 %_17, 1
	%_23 = getelementptr i32, i32* %_16, i32 %_22
	%_24 = load i32, i32* %i
	store i32 %_24, i32* %_23
	 br label %oob21
	oob20:
	call void @throw_oob()
	 br label %oob21
	oob21:
	%_25 = load i32*, i32** %b
	%_26 = load i32, i32* %i
	%_27 = load i32, i32* %_25
	%_28 = icmp ult i32 %_26, %_27
	br i1 %_28, label %oob32, label %oob33
	oob32:
	%_29 = add i32 %_26, 1
	%_30 = getelementptr i32, i32* %_25, i32 %_29
	%_31 = load i32, i32* %_30
	br label %oob34
	oob33:
	call void @throw_oob()
	br label %oob34
	oob34:
	call void (i32) @print_int(i32 %_31)
	%_35 = load i32, i32* %i
	%_36 = add i32 %_35, 1
	store i32 %_36, i32* %i

	br label %loop9

	loop11:
	ret i1 1
}

