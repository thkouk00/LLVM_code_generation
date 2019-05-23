@.Example1_vtable = global [0 x i8*] []
@.Test1_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32, i1)* @Test1.Start to i8*)]

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
	%_0 = call i8* @calloc(i32 1, i32 12)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Test1_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	; Test1.Start : 0
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32, i1)*
	%_8 = call i32 %_7(i8* %_0, i32 5, i1 1)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @Test1.Start(i8* %this, i32 %.b, i1 %.c) {
	%b = alloca i32
	store i32 %.b, i32* %b
	%c = alloca i1
	store i1 %.c, i1* %c
	%ntb = alloca i1
	%nti = alloca i32*
	%ourint = alloca i32
	%_0 = load i32, i32* %b
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
	store i32* %_2, i32** %nti
	%_7 = load i32*, i32** %nti
	%_8 = load i32, i32* %_7
	%_9 = icmp ult i32 0, %_8
	br i1 %_9, label %oob13, label %oob14
	oob13:
	%_10 = add i32 0, 1
	%_11 = getelementptr i32, i32* %_7, i32 %_10
	%_12 = load i32, i32* %_11
	br label %oob15
	oob14:
	call void @throw_oob()
	br label %oob15
	oob15:
	store i32 %_12, i32* %ourint
	%_16 = load i32, i32* %ourint
	call void (i32) @print_int(i32 %_16)
	%_17 = load i32*, i32** %nti
	%_18 = load i32, i32* %_17
	%_19 = icmp ult i32 0, %_18
	br i1 %_19, label %oob23, label %oob24
	oob23:
	%_20 = add i32 0, 1
	%_21 = getelementptr i32, i32* %_17, i32 %_20
	%_22 = load i32, i32* %_21
	br label %oob25
	oob24:
	call void @throw_oob()
	br label %oob25
	oob25:
	ret i32 %_22
}

