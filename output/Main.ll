@.Main_vtable = global [0 x i8*] []
@.ArrayTest_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32)* @ArrayTest.test to i8*)]

@.B_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32)* @B.test to i8*)]

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
	%ab = alloca i8*
	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.ArrayTest_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	store i8* %_0, i8** %ab
	%_3 = load i8*, i8** %ab
	; ArrayTest.test : 0
	%_4 = bitcast i8* %_3 to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 0
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*, i32)*
	%_9 = call i32 %_8(i8* %_3, i32 3)
	call void (i32) @print_int(i32 %_9)


	ret i32 0
}

define i32 @ArrayTest.test(i8* %this, i32 %.num) {
	%num = alloca i32
	store i32 %.num, i32* %num
	%i = alloca i32
	%intArray = alloca i32*
	%_0 = load i32, i32* %num
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
	store i32* %_2, i32** %intArray
	%_7 = getelementptr i8, i8* %this, i32 16
	%_8 = bitcast i8* %_7 to i32*
	store i32 0, i32* %_8
	%_9 = getelementptr i8, i8* %this, i32 16
	%_10 = bitcast i8* %_9 to i32*
	%_11 = load i32, i32* %_10
	call void (i32) @print_int(i32 %_11)
	%_13 = load i32*, i32** %intArray
	%_12 = load i32, i32* %_13
	call void (i32) @print_int(i32 %_12)
	store i32 0, i32* %i
	call void (i32) @print_int(i32 111)
	br label %loop14
	loop14:
	%_17 = load i32, i32* %i
	%_19 = load i32*, i32** %intArray
	%_18 = load i32, i32* %_19
	%_20 = icmp slt i32 %_17, %_18
	br i1 %_20, label %loop15, label %loop16

	loop15:
	%_21 = load i32, i32* %i
	%_22 = add i32 %_21, 1
	call void (i32) @print_int(i32 %_22)
	%_24 = load i32*, i32** %intArray
	%_23 = load i32 , i32* %_24
	%_25 = load i32, i32* %i
	%_26 = icmp ult i32 %_25, %_23
	br i1 %_26, label %oob27, label %oob28
	oob27:
	%_30 = add i32 %_25, 1
	%_31 = getelementptr i32, i32* %_24, i32 %_30
	%_32 = load i32, i32* %i
	%_33 = add i32 %_32, 1
	store i32 %_33, i32* %_31
	 br label %oob29
	oob28:
	call void @throw_oob()
	 br label %oob29
	oob29:
	%_34 = load i32, i32* %i
	%_35 = add i32 %_34, 1
	store i32 %_35, i32* %i

	br label %loop14

	loop16:
	call void (i32) @print_int(i32 222)
	store i32 0, i32* %i
	br label %loop36
	loop36:
	%_39 = load i32, i32* %i
	%_41 = load i32*, i32** %intArray
	%_40 = load i32, i32* %_41
	%_42 = icmp slt i32 %_39, %_40
	br i1 %_42, label %loop37, label %loop38

	loop37:
	%_43 = load i32*, i32** %intArray
	%_44 = load i32, i32* %i
	%_45 = load i32, i32* %_43
	%_46 = icmp ult i32 %_44, %_45
	br i1 %_46, label %oob50, label %oob51
	oob50:
	%_47 = add i32 %_44, 1
	%_48 = getelementptr i32, i32* %_43, i32 %_47
	%_49 = load i32, i32* %_48
	br label %oob52
	oob51:
	call void @throw_oob()
	br label %oob52
	oob52:
	call void (i32) @print_int(i32 %_49)
	%_53 = load i32, i32* %i
	%_54 = add i32 %_53, 1
	store i32 %_54, i32* %i

	br label %loop36

	loop38:
	call void (i32) @print_int(i32 333)
	%_56 = load i32*, i32** %intArray
	%_55 = load i32, i32* %_56
	ret i32 %_55
}

define i32 @B.test(i8* %this, i32 %.num) {
	%num = alloca i32
	store i32 %.num, i32* %num
	%i = alloca i32
	%intArray = alloca i32*
	%_0 = load i32, i32* %num
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
	store i32* %_2, i32** %intArray
	%_7 = getelementptr i8, i8* %this, i32 20
	%_8 = bitcast i8* %_7 to i32*
	store i32 12, i32* %_8
	%_9 = getelementptr i8, i8* %this, i32 20
	%_10 = bitcast i8* %_9 to i32*
	%_11 = load i32, i32* %_10
	call void (i32) @print_int(i32 %_11)
	%_13 = load i32*, i32** %intArray
	%_12 = load i32, i32* %_13
	call void (i32) @print_int(i32 %_12)
	store i32 0, i32* %i
	call void (i32) @print_int(i32 111)
	br label %loop14
	loop14:
	%_17 = load i32, i32* %i
	%_19 = load i32*, i32** %intArray
	%_18 = load i32, i32* %_19
	%_20 = icmp slt i32 %_17, %_18
	br i1 %_20, label %loop15, label %loop16

	loop15:
	%_21 = load i32, i32* %i
	%_22 = add i32 %_21, 1
	call void (i32) @print_int(i32 %_22)
	%_24 = load i32*, i32** %intArray
	%_23 = load i32 , i32* %_24
	%_25 = load i32, i32* %i
	%_26 = icmp ult i32 %_25, %_23
	br i1 %_26, label %oob27, label %oob28
	oob27:
	%_30 = add i32 %_25, 1
	%_31 = getelementptr i32, i32* %_24, i32 %_30
	%_32 = load i32, i32* %i
	%_33 = add i32 %_32, 1
	store i32 %_33, i32* %_31
	 br label %oob29
	oob28:
	call void @throw_oob()
	 br label %oob29
	oob29:
	%_34 = load i32, i32* %i
	%_35 = add i32 %_34, 1
	store i32 %_35, i32* %i

	br label %loop14

	loop16:
	call void (i32) @print_int(i32 222)
	store i32 0, i32* %i
	br label %loop36
	loop36:
	%_39 = load i32, i32* %i
	%_41 = load i32*, i32** %intArray
	%_40 = load i32, i32* %_41
	%_42 = icmp slt i32 %_39, %_40
	br i1 %_42, label %loop37, label %loop38

	loop37:
	%_43 = load i32*, i32** %intArray
	%_44 = load i32, i32* %i
	%_45 = load i32, i32* %_43
	%_46 = icmp ult i32 %_44, %_45
	br i1 %_46, label %oob50, label %oob51
	oob50:
	%_47 = add i32 %_44, 1
	%_48 = getelementptr i32, i32* %_43, i32 %_47
	%_49 = load i32, i32* %_48
	br label %oob52
	oob51:
	call void @throw_oob()
	br label %oob52
	oob52:
	call void (i32) @print_int(i32 %_49)
	%_53 = load i32, i32* %i
	%_54 = add i32 %_53, 1
	store i32 %_54, i32* %i

	br label %loop36

	loop38:
	call void (i32) @print_int(i32 333)
	%_56 = load i32*, i32** %intArray
	%_55 = load i32, i32* %_56
	ret i32 %_55
}

