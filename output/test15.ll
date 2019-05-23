@.test15_vtable = global [0 x i8*] []
@.Test_vtable = global [3 x i8*] [i8* bitcast (i32 (i8*)* @Test.start to i8*), i8* bitcast (i32 (i8*)* @Test.mutual1 to i8*), i8* bitcast (i32 (i8*)* @Test.mutual2 to i8*)]

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
	%_0 = call i8* @calloc(i32 1, i32 16)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [3 x i8*], [3 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	; Test.start : 0
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*)*
	%_8 = call i32 %_7(i8* %_0)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @Test.start(i8* %this) {
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	store i32 4, i32* %_1
	%_2 = getelementptr i8, i8* %this, i32 12
	%_3 = bitcast i8* %_2 to i32*
	store i32 0, i32* %_3
	; Test.mutual1 : 1
	%_4 = bitcast i8* %this to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 1
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*)*
	%_9 = call i32 %_8(i8* %this)
	ret i32 %_9
}

define i32 @Test.mutual1(i8* %this) {
	%j = alloca i32
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	%_3 = sub i32 %_2, 1
	%_4 = getelementptr i8, i8* %this, i32 8
	%_5 = bitcast i8* %_4 to i32*
	store i32 %_3, i32* %_5
	%_9 = getelementptr i8, i8* %this, i32 8
	%_10 = bitcast i8* %_9 to i32*
	%_11 = load i32, i32* %_10
	%_12 = icmp slt i32 %_11, 0
	br i1 %_12, label %if6, label %if7

if6: 
	%_13 = getelementptr i8, i8* %this, i32 12
	%_14 = bitcast i8* %_13 to i32*
	store i32 0, i32* %_14

	br label %if8

if7: 
	%_15 = getelementptr i8, i8* %this, i32 12
	%_16 = bitcast i8* %_15 to i32*
	%_17 = load i32, i32* %_16
	call void (i32) @print_int(i32 %_17)
	%_18 = getelementptr i8, i8* %this, i32 12
	%_19 = bitcast i8* %_18 to i32*
	store i32 1, i32* %_19
	; Test.mutual2 : 2
	%_20 = bitcast i8* %this to i8***
	%_21 = load i8**, i8*** %_20
	%_22 = getelementptr i8*, i8** %_21, i32 2
	%_23 = load i8*, i8** %_22
	%_24 = bitcast i8* %_23 to i32 (i8*)*
	%_25 = call i32 %_24(i8* %this)
	store i32 %_25, i32* %j

	br label %if8

if8: 
	%_26 = getelementptr i8, i8* %this, i32 12
	%_27 = bitcast i8* %_26 to i32*
	%_28 = load i32, i32* %_27
	ret i32 %_28
}

define i32 @Test.mutual2(i8* %this) {
	%j = alloca i32
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	%_3 = sub i32 %_2, 1
	%_4 = getelementptr i8, i8* %this, i32 8
	%_5 = bitcast i8* %_4 to i32*
	store i32 %_3, i32* %_5
	%_9 = getelementptr i8, i8* %this, i32 8
	%_10 = bitcast i8* %_9 to i32*
	%_11 = load i32, i32* %_10
	%_12 = icmp slt i32 %_11, 0
	br i1 %_12, label %if6, label %if7

if6: 
	%_13 = getelementptr i8, i8* %this, i32 12
	%_14 = bitcast i8* %_13 to i32*
	store i32 0, i32* %_14

	br label %if8

if7: 
	%_15 = getelementptr i8, i8* %this, i32 12
	%_16 = bitcast i8* %_15 to i32*
	%_17 = load i32, i32* %_16
	call void (i32) @print_int(i32 %_17)
	%_18 = getelementptr i8, i8* %this, i32 12
	%_19 = bitcast i8* %_18 to i32*
	store i32 0, i32* %_19
	; Test.mutual1 : 1
	%_20 = bitcast i8* %this to i8***
	%_21 = load i8**, i8*** %_20
	%_22 = getelementptr i8*, i8** %_21, i32 1
	%_23 = load i8*, i8** %_22
	%_24 = bitcast i8* %_23 to i32 (i8*)*
	%_25 = call i32 %_24(i8* %this)
	store i32 %_25, i32* %j

	br label %if8

if8: 
	%_26 = getelementptr i8, i8* %this, i32 12
	%_27 = bitcast i8* %_26 to i32*
	%_28 = load i32, i32* %_27
	ret i32 %_28
}

