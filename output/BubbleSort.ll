@.BubbleSort_vtable = global [0 x i8*] []
@.BBS_vtable = global [4 x i8*] [i8* bitcast (i32 (i8*, i32)* @BBS.Start to i8*), i8* bitcast (i32 (i8*)* @BBS.Sort to i8*), i8* bitcast (i32 (i8*)* @BBS.Print to i8*), i8* bitcast (i32 (i8*, i32)* @BBS.Init to i8*)]

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
	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [4 x i8*], [4 x i8*]* @.BBS_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	; BBS.Start : 0
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32)*
	%_8 = call i32 %_7(i8* %_0, i32 10)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @BBS.Start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%aux01 = alloca i32
	; BBS.Init : 3
	%_0 = bitcast i8* %this to i8***
	%_1 = load i8**, i8*** %_0
	%_2 = getelementptr i8*, i8** %_1, i32 3
	%_3 = load i8*, i8** %_2
	%_4 = bitcast i8* %_3 to i32 (i8*, i32)*
	%_6 = load i32, i32* %sz
	%_5 = call i32 %_4(i8* %this, i32 %_6)
	store i32 %_5, i32* %aux01
	; BBS.Print : 2
	%_7 = bitcast i8* %this to i8***
	%_8 = load i8**, i8*** %_7
	%_9 = getelementptr i8*, i8** %_8, i32 2
	%_10 = load i8*, i8** %_9
	%_11 = bitcast i8* %_10 to i32 (i8*)*
	%_12 = call i32 %_11(i8* %this)
	store i32 %_12, i32* %aux01
	call void (i32) @print_int(i32 99999)
	; BBS.Sort : 1
	%_13 = bitcast i8* %this to i8***
	%_14 = load i8**, i8*** %_13
	%_15 = getelementptr i8*, i8** %_14, i32 1
	%_16 = load i8*, i8** %_15
	%_17 = bitcast i8* %_16 to i32 (i8*)*
	%_18 = call i32 %_17(i8* %this)
	store i32 %_18, i32* %aux01
	; BBS.Print : 2
	%_19 = bitcast i8* %this to i8***
	%_20 = load i8**, i8*** %_19
	%_21 = getelementptr i8*, i8** %_20, i32 2
	%_22 = load i8*, i8** %_21
	%_23 = bitcast i8* %_22 to i32 (i8*)*
	%_24 = call i32 %_23(i8* %this)
	store i32 %_24, i32* %aux01
	ret i32 0
}

define i32 @BBS.Sort(i8* %this) {
	%nt = alloca i32
	%i = alloca i32
	%aux02 = alloca i32
	%aux04 = alloca i32
	%aux05 = alloca i32
	%aux06 = alloca i32
	%aux07 = alloca i32
	%j = alloca i32
	%t = alloca i32
	%_0 = getelementptr i8, i8* %this, i32 16
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	%_3 = sub i32 %_2, 1
	store i32 %_3, i32* %i
	%_4 = sub i32 0, 1
	store i32 %_4, i32* %aux02
	br label %loop5
	loop5:
	%_8 = load i32, i32* %aux02
	%_9 = load i32, i32* %i
	%_10 = icmp slt i32 %_8, %_9
	br i1 %_10, label %loop6, label %loop7

	loop6:
	store i32 1, i32* %j
	br label %loop11
	loop11:
	%_14 = load i32, i32* %j
	%_15 = load i32, i32* %i
	%_16 = add i32 %_15, 1
	%_17 = icmp slt i32 %_14, %_16
	br i1 %_17, label %loop12, label %loop13

	loop12:
	%_18 = load i32, i32* %j
	%_19 = sub i32 %_18, 1
	store i32 %_19, i32* %aux07
	%_20 = getelementptr i8, i8* %this, i32 8
	%_21 = bitcast i8* %_20 to i32**
	%_22 = load i32*, i32** %_21
	%_23 = load i32, i32* %aux07
	%_24 = load i32, i32* %_22
	%_25 = icmp ult i32 %_23, %_24
	br i1 %_25, label %oob29, label %oob30
	oob29:
	%_26 = add i32 %_23, 1
	%_27 = getelementptr i32, i32* %_22, i32 %_26
	%_28 = load i32, i32* %_27
	br label %oob31
	oob30:
	call void @throw_oob()
	br label %oob31
	oob31:
	store i32 %_28, i32* %aux04
	%_32 = getelementptr i8, i8* %this, i32 8
	%_33 = bitcast i8* %_32 to i32**
	%_34 = load i32*, i32** %_33
	%_35 = load i32, i32* %j
	%_36 = load i32, i32* %_34
	%_37 = icmp ult i32 %_35, %_36
	br i1 %_37, label %oob41, label %oob42
	oob41:
	%_38 = add i32 %_35, 1
	%_39 = getelementptr i32, i32* %_34, i32 %_38
	%_40 = load i32, i32* %_39
	br label %oob43
	oob42:
	call void @throw_oob()
	br label %oob43
	oob43:
	store i32 %_40, i32* %aux05
	%_47 = load i32, i32* %aux05
	%_48 = load i32, i32* %aux04
	%_49 = icmp slt i32 %_47, %_48
	br i1 %_49, label %if44, label %if45

if44: 
	%_50 = load i32, i32* %j
	%_51 = sub i32 %_50, 1
	store i32 %_51, i32* %aux06
	%_52 = getelementptr i8, i8* %this, i32 8
	%_53 = bitcast i8* %_52 to i32**
	%_54 = load i32*, i32** %_53
	%_55 = load i32, i32* %aux06
	%_56 = load i32, i32* %_54
	%_57 = icmp ult i32 %_55, %_56
	br i1 %_57, label %oob61, label %oob62
	oob61:
	%_58 = add i32 %_55, 1
	%_59 = getelementptr i32, i32* %_54, i32 %_58
	%_60 = load i32, i32* %_59
	br label %oob63
	oob62:
	call void @throw_oob()
	br label %oob63
	oob63:
	store i32 %_60, i32* %t
	%_65 = getelementptr i8, i8* %this, i32 8
	%_66 = bitcast i8* %_65 to i32**
	%_67 = load i32*, i32** %_66
	%_64 = load i32, i32* %_67
	%_68 = load i32, i32* %aux06
	%_69 = icmp ult i32 %_68, %_64
	br i1 %_69, label %oob70, label %oob71
	oob70:
	%_73 = add i32 %_68, 1
	%_74 = getelementptr i32, i32* %_67, i32 %_73
	%_75 = getelementptr i8, i8* %this, i32 8
	%_76 = bitcast i8* %_75 to i32**
	%_77 = load i32*, i32** %_76
	%_78 = load i32, i32* %j
	%_79 = load i32, i32* %_77
	%_80 = icmp ult i32 %_78, %_79
	br i1 %_80, label %oob84, label %oob85
	oob84:
	%_81 = add i32 %_78, 1
	%_82 = getelementptr i32, i32* %_77, i32 %_81
	%_83 = load i32, i32* %_82
	br label %oob86
	oob85:
	call void @throw_oob()
	br label %oob86
	oob86:
	store i32 %_83, i32* %_74
	 br label %oob72
	oob71:
	call void @throw_oob()
	 br label %oob72
	oob72:
	%_88 = getelementptr i8, i8* %this, i32 8
	%_89 = bitcast i8* %_88 to i32**
	%_90 = load i32*, i32** %_89
	%_87 = load i32, i32* %_90
	%_91 = load i32, i32* %j
	%_92 = icmp ult i32 %_91, %_87
	br i1 %_92, label %oob93, label %oob94
	oob93:
	%_96 = add i32 %_91, 1
	%_97 = getelementptr i32, i32* %_90, i32 %_96
	%_98 = load i32, i32* %t
	store i32 %_98, i32* %_97
	 br label %oob95
	oob94:
	call void @throw_oob()
	 br label %oob95
	oob95:

	br label %if46

if45: 
	store i32 0, i32* %nt

	br label %if46

if46: 
	%_99 = load i32, i32* %j
	%_100 = add i32 %_99, 1
	store i32 %_100, i32* %j

	br label %loop11

	loop13:
	%_101 = load i32, i32* %i
	%_102 = sub i32 %_101, 1
	store i32 %_102, i32* %i

	br label %loop5

	loop7:
	ret i32 0
}

define i32 @BBS.Print(i8* %this) {
	%j = alloca i32
	store i32 0, i32* %j
	br label %loop0
	loop0:
	%_3 = load i32, i32* %j
	%_4 = getelementptr i8, i8* %this, i32 16
	%_5 = bitcast i8* %_4 to i32*
	%_6 = load i32, i32* %_5
	%_7 = icmp slt i32 %_3, %_6
	br i1 %_7, label %loop1, label %loop2

	loop1:
	%_8 = getelementptr i8, i8* %this, i32 8
	%_9 = bitcast i8* %_8 to i32**
	%_10 = load i32*, i32** %_9
	%_11 = load i32, i32* %j
	%_12 = load i32, i32* %_10
	%_13 = icmp ult i32 %_11, %_12
	br i1 %_13, label %oob17, label %oob18
	oob17:
	%_14 = add i32 %_11, 1
	%_15 = getelementptr i32, i32* %_10, i32 %_14
	%_16 = load i32, i32* %_15
	br label %oob19
	oob18:
	call void @throw_oob()
	br label %oob19
	oob19:
	call void (i32) @print_int(i32 %_16)
	%_20 = load i32, i32* %j
	%_21 = add i32 %_20, 1
	store i32 %_21, i32* %j

	br label %loop0

	loop2:
	ret i32 0
}

define i32 @BBS.Init(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%_0 = load i32, i32* %sz
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2
	%_3 = load i32, i32* %sz
	%_6 = icmp slt i32 %_3, 0
	br i1 %_6, label %oob8, label %oob9
	oob8:
	call void @throw_oob()
	br label %oob9
	oob9:
		%_7 = add i32 %_3, 1
	%_4 = call i8* @calloc(i32 4, i32 %_7)
	%_5 = bitcast i8* %_4 to i32*
	store i32 %_3, i32* %_5
	%_10 = getelementptr i8, i8* %this, i32 8
	%_11 = bitcast i8* %_10 to i32**
	store i32* %_5, i32** %_11
	%_13 = getelementptr i8, i8* %this, i32 8
	%_14 = bitcast i8* %_13 to i32**
	%_15 = load i32*, i32** %_14
	%_12 = load i32, i32* %_15
	%_16 = icmp ult i32 0, %_12
	br i1 %_16, label %oob17, label %oob18
	oob17:
	%_20 = add i32 0, 1
	%_21 = getelementptr i32, i32* %_15, i32 %_20
	store i32 20, i32* %_21
	 br label %oob19
	oob18:
	call void @throw_oob()
	 br label %oob19
	oob19:
	%_23 = getelementptr i8, i8* %this, i32 8
	%_24 = bitcast i8* %_23 to i32**
	%_25 = load i32*, i32** %_24
	%_22 = load i32, i32* %_25
	%_26 = icmp ult i32 1, %_22
	br i1 %_26, label %oob27, label %oob28
	oob27:
	%_30 = add i32 1, 1
	%_31 = getelementptr i32, i32* %_25, i32 %_30
	store i32 7, i32* %_31
	 br label %oob29
	oob28:
	call void @throw_oob()
	 br label %oob29
	oob29:
	%_33 = getelementptr i8, i8* %this, i32 8
	%_34 = bitcast i8* %_33 to i32**
	%_35 = load i32*, i32** %_34
	%_32 = load i32, i32* %_35
	%_36 = icmp ult i32 2, %_32
	br i1 %_36, label %oob37, label %oob38
	oob37:
	%_40 = add i32 2, 1
	%_41 = getelementptr i32, i32* %_35, i32 %_40
	store i32 12, i32* %_41
	 br label %oob39
	oob38:
	call void @throw_oob()
	 br label %oob39
	oob39:
	%_43 = getelementptr i8, i8* %this, i32 8
	%_44 = bitcast i8* %_43 to i32**
	%_45 = load i32*, i32** %_44
	%_42 = load i32, i32* %_45
	%_46 = icmp ult i32 3, %_42
	br i1 %_46, label %oob47, label %oob48
	oob47:
	%_50 = add i32 3, 1
	%_51 = getelementptr i32, i32* %_45, i32 %_50
	store i32 18, i32* %_51
	 br label %oob49
	oob48:
	call void @throw_oob()
	 br label %oob49
	oob49:
	%_53 = getelementptr i8, i8* %this, i32 8
	%_54 = bitcast i8* %_53 to i32**
	%_55 = load i32*, i32** %_54
	%_52 = load i32, i32* %_55
	%_56 = icmp ult i32 4, %_52
	br i1 %_56, label %oob57, label %oob58
	oob57:
	%_60 = add i32 4, 1
	%_61 = getelementptr i32, i32* %_55, i32 %_60
	store i32 2, i32* %_61
	 br label %oob59
	oob58:
	call void @throw_oob()
	 br label %oob59
	oob59:
	%_63 = getelementptr i8, i8* %this, i32 8
	%_64 = bitcast i8* %_63 to i32**
	%_65 = load i32*, i32** %_64
	%_62 = load i32, i32* %_65
	%_66 = icmp ult i32 5, %_62
	br i1 %_66, label %oob67, label %oob68
	oob67:
	%_70 = add i32 5, 1
	%_71 = getelementptr i32, i32* %_65, i32 %_70
	store i32 11, i32* %_71
	 br label %oob69
	oob68:
	call void @throw_oob()
	 br label %oob69
	oob69:
	%_73 = getelementptr i8, i8* %this, i32 8
	%_74 = bitcast i8* %_73 to i32**
	%_75 = load i32*, i32** %_74
	%_72 = load i32, i32* %_75
	%_76 = icmp ult i32 6, %_72
	br i1 %_76, label %oob77, label %oob78
	oob77:
	%_80 = add i32 6, 1
	%_81 = getelementptr i32, i32* %_75, i32 %_80
	store i32 6, i32* %_81
	 br label %oob79
	oob78:
	call void @throw_oob()
	 br label %oob79
	oob79:
	%_83 = getelementptr i8, i8* %this, i32 8
	%_84 = bitcast i8* %_83 to i32**
	%_85 = load i32*, i32** %_84
	%_82 = load i32, i32* %_85
	%_86 = icmp ult i32 7, %_82
	br i1 %_86, label %oob87, label %oob88
	oob87:
	%_90 = add i32 7, 1
	%_91 = getelementptr i32, i32* %_85, i32 %_90
	store i32 9, i32* %_91
	 br label %oob89
	oob88:
	call void @throw_oob()
	 br label %oob89
	oob89:
	%_93 = getelementptr i8, i8* %this, i32 8
	%_94 = bitcast i8* %_93 to i32**
	%_95 = load i32*, i32** %_94
	%_92 = load i32, i32* %_95
	%_96 = icmp ult i32 8, %_92
	br i1 %_96, label %oob97, label %oob98
	oob97:
	%_100 = add i32 8, 1
	%_101 = getelementptr i32, i32* %_95, i32 %_100
	store i32 19, i32* %_101
	 br label %oob99
	oob98:
	call void @throw_oob()
	 br label %oob99
	oob99:
	%_103 = getelementptr i8, i8* %this, i32 8
	%_104 = bitcast i8* %_103 to i32**
	%_105 = load i32*, i32** %_104
	%_102 = load i32, i32* %_105
	%_106 = icmp ult i32 9, %_102
	br i1 %_106, label %oob107, label %oob108
	oob107:
	%_110 = add i32 9, 1
	%_111 = getelementptr i32, i32* %_105, i32 %_110
	store i32 5, i32* %_111
	 br label %oob109
	oob108:
	call void @throw_oob()
	 br label %oob109
	oob109:
	ret i32 0
}

