@.OutOfBounds1_vtable = global [0 x i8*] []
@.A_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*)* @A.run to i8*)]

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
	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.A_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	; A.run : 0
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*)*
	%_8 = call i32 %_7(i8* %_0)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @A.run(i8* %this) {
	%a = alloca i32*
	%_2 = icmp slt i32 20, 0
	br i1 %_2, label %oob4, label %oob5
	oob4:
	call void @throw_oob()
	br label %oob5
	oob5:
		%_3 = add i32 20, 1
	%_0 = call i8* @calloc(i32 4, i32 %_3)
	%_1 = bitcast i8* %_0 to i32*
	store i32 20, i32* %_1
	store i32* %_1, i32** %a
	%_6 = load i32*, i32** %a
	%_7 = load i32, i32* %_6
	%_8 = icmp ult i32 10, %_7
	br i1 %_8, label %oob12, label %oob13
	oob12:
	%_9 = add i32 10, 1
	%_10 = getelementptr i32, i32* %_6, i32 %_9
	%_11 = load i32, i32* %_10
	br label %oob14
	oob13:
	call void @throw_oob()
	br label %oob14
	oob14:
	call void (i32) @print_int(i32 %_11)
	%_15 = load i32*, i32** %a
	%_16 = load i32, i32* %_15
	%_17 = icmp ult i32 40, %_16
	br i1 %_17, label %oob21, label %oob22
	oob21:
	%_18 = add i32 40, 1
	%_19 = getelementptr i32, i32* %_15, i32 %_18
	%_20 = load i32, i32* %_19
	br label %oob23
	oob22:
	call void @throw_oob()
	br label %oob23
	oob23:
	ret i32 %_20
}

