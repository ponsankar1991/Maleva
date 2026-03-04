USE [LiveMaleva2]
GO

/****** Object:  StoredProcedure [dbo].[SP_SaleOrderMaster]    Script Date: 3/3/2026 2:49:44 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO





CREATE PROCEDURE [dbo].[SP_SaleOrderMaster]
	@master nvarchar(max),
	@Comid int
AS
BEGIN
    declare	@Result int=0;
	declare @msg varchar(100)='';
	declare @count int=0;
	declare @countcheck int=0;
	declare @flag int=1;
	declare @saleid int=0;
	declare @SRIdNew int=0;
	declare @StartBillNo int =0;
	declare @RefNo int=0;
	BEGIN TRY	
	    BEGIN TRANSACTION	
        IF OBJECT_ID('tempdb..#temp1') IS NOT NULL    drop TABLE tempdb..#temp1

	    SELECT * 
         INTO #temp1
         FROM 
		 (SELECT ROW_NUMBER() OVER(ORDER BY SNo) AS tempid,Id,sportsaleorderid,PortCPop,Notportchagre,NotBoatCPop,NotBoatCPop1,NotForwardingCPop,NotPermitCPop,NotLevyChares,NotMMHECPop,NotAFpoCPop,NotSFWpoCPop,NotSFEWpoCPop,NotPFPPCPop1,ForwardingCPop,BoatCPop,BoatCPop1,PermitCPop,LiveCPop,MMHECPop,AFpoCPop,SFWpoCPop,SFEWpoCPop,PFPPCPop1,rbtportchagdeop,CompanyRefId,CustomerRefId,EmployeeRefId,UserRefId,JobMasterRefId,AgentMasterRefId,AgentCompanyRefId,OAgentMasterRefId,OAgentCompanyRefId,BillType,SaleDate,SaleType,GrossAmount,TaxAmount,DiscountAmount,Remarks,Remarks1,DODescription,PlusAmount,MinusAmount,Coinage,Amount,Offvesselname,Loadingvesselname,TruckSize,
		 SPort,OPort,SCN,ETA,ETB,ETD,OETA,OETB,OETD,DOCNo,InvoiceNo,TruckRefid,DriverRefid,Vessel,OVessel,Commodity,Cargo,AWBNo,BLCopy,Quantity,TotalWeight,JStatus,OStatus,ForkliftbyRefid,SealbyRefid,SealbreakbyRefid,BoardingOfficerRefid,BoardingOfficer1Refid,BoardingAmount,BoardingAmount1,ForwardingEnterRef,ForwardingExitRef,ForwardingEnterRef2,ForwardingExitRef2,ForwardingEnterRef3,ForwardingExitRef3,PortChargesRef,PortCharges, SealAmount,BreakSealAmount,SealAmount2,BreakSealAmount2,SealAmount3,BreakSealAmount3,SealbyRefid2,SealbreakbyRefid2,SealbyRefid3,SealbreakbyRefid3,LSCN,PickupDate,ForwardingSMKNo,ForwardingSMKNo2,ForwardingSMKNo3,
DeliveryDate,WareHouseEnterDate,WareHouseExitDate,PickupAddress,pickuptimelist,pickupQuantityList,DeliveryQuantityList,DelivertimeList, Quantitylist,DeliveryAddress,WareHouseAddress,Forwarding,Forwarding2,Forwarding3,ForwardingQuantity,ForwardingQuantity2,ForwardingQuantity3,Origin,Destination,Zb,PTW,Zb2,ZbRef,ZbRef2,CNumberDisplay,CNumber,CurrencyValue,ActualNetAmount,Forwarding1S1,Forwarding1S2,Forwarding2S1,Forwarding2S2,Forwarding3S1,Forwarding3S2,trucksize2,OriginRefId,DestinationRefId,ForwardingDate,Forwarding2Date,Forwarding3Date,LBoardingOfficerRefid,LBoardingOfficer1Refid,LBoardingAmount,LBoardingAmount1,LPortChargesRef,LPortCharges,OBoardingOfficerRefid,OBoardingOfficer1Refid,OBoardingAmount,OBoardingAmount1,OPortChargesRef,OPortCharges,LPTW,OPTW,FlighTime,SymbolRefId,SaleDetails
		  FROM OPENJSON(@master)
        WITH (
		SNo int '$.boundindex', 
		Id int '$.Id',
		sportsaleorderid int '$.sportsaleorderid',
		PortCPop int '$.PortCPop',
		LiveCPop int '$.LiveCPop',
		Notportchagre int '$.Notportchagre',
		NotBoatCPop int '$.NotBoatCPop',
		NotBoatCPop1 int '$.NotBoatCPop1',
		NotForwardingCPop int '$.NotForwardingCPop',
		NotLevyChares int '$.NotLevyChares',
		NotMMHECPop int '$.NotMMHECPop',
		NotAFpoCPop int '$.NotAFpoCPop',
		NotSFWpoCPop int '$.NotSFWpoCPop',
		NotSFEWpoCPop int '$.NotSFEWpoCPop',
		NotPFPPCPop1 int '$.NotPFPPCPop1',
		NotPermitCPop int '$.NotPermitCPop',
		ForwardingCPop int '$.ForwardingCPop',
		BoatCPop int '$.BoatCPop',
		PermitCPop int '$.PermitCPop',
		MMHECPop int '$.MMHECPop',
		AFpoCPop int '$.AFpoCPop',
		BoatCPop1 int '$.BoatCPop1',
		SFWpoCPop int '$.SFWpoCPop',
		PFPPCPop1 int '$.PFPPCPop1',
		rbtportchagdeop int '$.rbtportchagdeop',
		SFEWpoCPop int '$.SFEWpoCPop',
		CompanyRefId int '$.CompanyRefId',
        CustomerRefId int '$.CustomerRefId',	
		 EmployeeRefId int '$.EmployeeRefId',	
		 UserRefId int '$.UserRefId',	
		 JobMasterRefId int '$.JobMasterRefId',	
		 AgentMasterRefId int '$.AgentMasterRefId',	
		 AgentCompanyRefId int '$.AgentCompanyRefId',	
		 OAgentMasterRefId int '$.OAgentMasterRefId',	
		 OAgentCompanyRefId int '$.OAgentCompanyRefId',	

		 	BillType varchar(50) '$.BillType',
		 
		 
		SaleDate date '$.SaleDate',
		SaleType varchar(20) '$.SaleType',
		GrossAmount real '$.GrossAmount',
		CurrencyValue real '$.CurrencyValue',
		ActualNetAmount real '$.ActualNetAmount',
		
		TaxAmount real '$.TaxAmount',
		DiscountAmount real '$.DiscountAmount',
		Remarks varchar(300) '$.Remarks',
		Remarks1 varchar(300) '$.Remarks1',
		
		DODescription varchar(500) '$.DODescription',
		
		
		
	    PlusAmount real '$.PlusAmount',
		MinusAmount real '$.MinusAmount',
		Coinage real '$.Coinage',
		Amount real '$.Amount',
		Offvesselname varchar(200) '$.Offvesselname',
		Loadingvesselname varchar(200) '$.Loadingvesselname',
		TruckSize varchar(200) '$.TruckSize',
		
		SPort varchar(200) '$.SPort',
		OPort varchar(200) '$.OPort',
		
		SCN varchar(200) '$.SCN',
		LSCN varchar(200) '$.LSCN',
		ETA datetime '$.ETA',
		ETB datetime '$.ETB',
        ETD datetime '$.ETD',
		OETA datetime '$.OETA',
		OETB datetime '$.OETB',
        OETD datetime '$.OETD',
		DOCNo int '$.DOCNo',
		InvoiceNo int '$.InvoiceNo',
		TruckRefid int '$.TruckRefid',
		DriverRefid int '$.DriverRefid',
		Vessel varchar(200) '$.Vessel',
		OVessel varchar(200) '$.OVessel',
		
		Commodity varchar(100) '$.Commodity',
		Cargo varchar(100) '$.Cargo',
		
		AWBNo varchar(100) '$.AWBNo',
		BLCopy varchar(100) '$.BLCopy',
        Quantity varchar(100) '$.Quantity',
	TotalWeight varchar(100) '$.TotalWeight',
	JStatus int '$.JStatus',
	OStatus int '$.OStatus',
	ForkliftbyRefid int '$.ForkliftbyRefid',
	SealbyRefid int '$.SealbyRefid',
	SealbreakbyRefid int '$.SealbreakbyRefid',
	SealbyRefid2 int '$.SealbyRefid2',
	SealbreakbyRefid2 int '$.SealbreakbyRefid2',
	SealbyRefid3 int '$.SealbyRefid3',
	SealbreakbyRefid3 int '$.SealbreakbyRefid3',
	BoardingOfficerRefid int '$.BoardingOfficerRefid',
	BoardingOfficer1Refid int '$.BoardingOfficer1Refid',
	BoardingAmount real '$.BoardingAmount',
	BoardingAmount1 real '$.BoardingAmount1',
	ForwardingEnterRef varchar(200) '$.ForwardingEnterRef',
	ForwardingExitRef varchar(200) '$.ForwardingExitRef',
	ForwardingEnterRef2 varchar(200) '$.ForwardingEnterRef2',
	ForwardingExitRef2 varchar(200) '$.ForwardingExitRef2',
	ForwardingEnterRef3 varchar(200) '$.ForwardingEnterRef3',
	ForwardingExitRef3 varchar(200) '$.ForwardingExitRef3',
	ForwardingSMKNo varchar(200) '$.ForwardingSMKNo',
	ForwardingSMKNo2 varchar(200) '$.ForwardingSMKNo2',
	ForwardingSMKNo3 varchar(200) '$.ForwardingSMKNo3',
	
	ForwardingQuantity varchar(200) '$.ForwardingQuantity',
	ForwardingQuantity2 varchar(200) '$.ForwardingQuantity2 ',
	ForwardingQuantity3 varchar(200) '$.ForwardingQuantity3',

	PortChargesRef varchar(200) '$.PortChargesRef',
	PortCharges real '$.PortCharges',
	SealAmount real '$.SealAmount',
	BreakSealAmount real '$.BreakSealAmount',
	SealAmount2 real '$.SealAmount2',
	BreakSealAmount2 real '$.BreakSealAmount2',
	SealAmount3 real '$.SealAmount3',
	BreakSealAmount3 real '$.BreakSealAmount3',
	PickupDate datetime '$.PickupDate',	
	DeliveryDate datetime '$.DeliveryDate',
	WareHouseEnterDate datetime '$.WareHouseEnterDate',
	WareHouseExitDate datetime '$.WareHouseExitDate',
	WareHouseAddress varchar(2000) '$.WareHouseAddress',
	PickupAddress varchar(2000) '$.PickupAddress',
	pickupQuantityList varchar(5000) '$.pickupQuantityList',
	pickuptimelist varchar (5000) '$.pickuptimelist',
	DeliveryQuantityList varchar(5000)'$.DeliveryQuantityList',
	DelivertimeList varchar(5000)'$.DelivertimeList',
	DeliveryAddress varchar(2000) '$.DeliveryAddress',	
	Quantitylist varchar(500)'$.Quantitylist',
	Forwarding varchar(100) '$.Forwarding',
	Forwarding2 varchar(100) '$.Forwarding2',
	Forwarding3 varchar(100) '$.Forwarding3',
	Origin varchar(200) '$.Origin',
	Destination varchar(200) '$.Destination',	
	Zb varchar(100) '$.Zb',
	PTW varchar(100) '$.PTW',
	
	Zb2 varchar(100) '$.Zb2',
	ZbRef varchar(200) '$.ZbRef',
	ZbRef2 varchar(200) '$.ZbRef2',
	CNumberDisplay varchar(10) '$.CNumberDisplay',
	CNumber int '$.CNumber',
	Forwarding1S1 varchar(500) '$.Forwarding1S1',
	Forwarding1S2 varchar(500) '$.Forwarding1S2',
	Forwarding2S1 varchar(500) '$.Forwarding2S1',
	Forwarding2S2 varchar(500) '$.Forwarding2S2',
	Forwarding3S1 varchar(500) '$.Forwarding3S1',
	Forwarding3S2 varchar(500) '$.Forwarding3S2',
	trucksize2 varchar(500) '$.trucksize2',
		OriginRefId int '$.OriginRefId',
			DestinationRefId int '$.DestinationRefId',
			ForwardingDate datetime '$.ForwardingDate',
			Forwarding2Date datetime '$.Forwarding2Date',
			Forwarding3Date datetime '$.Forwarding3Date',
			LBoardingOfficerRefid int '$.LBoardingOfficerRefid',
	LBoardingOfficer1Refid int '$.LBoardingOfficer1Refid',
	LBoardingAmount real '$.LBoardingAmount',
	LBoardingAmount1 real '$.LBoardingAmount1',
	LPortChargesRef varchar(200) '$.LPortChargesRef',
	LPortCharges real '$.LPortCharges',
	OBoardingOfficerRefid int '$.OBoardingOfficerRefid',
	OBoardingOfficer1Refid int '$.OBoardingOfficer1Refid',
	OBoardingAmount real '$.OBoardingAmount',
	OBoardingAmount1 real '$.OBoardingAmount1',
	OPortChargesRef varchar(200) '$.OPortChargesRef',
	OPortCharges real '$.OPortCharges',
		LPTW varchar(100) '$.LPTW',
	OPTW varchar(100) '$.OPTW',
	FlighTime datetime '$.FlighTime',
	SymbolRefId int '$.SymbolRefId',
		SaleDetails nvarchar(max) '$.SaleDetails' AS JSON
		)) AS x

		declare @Id int 
		declare @sportsaleorderid int
		declare @PortCPop int
		declare @ForwardingCPop int
		declare @BoatCPop int
		declare @PermitCPop int
		declare @LiveCPop int
	   declare @MMHECPop int 
	   declare @AFpoCPop int
	   declare @PFPPCPop1 int 
	   declare @Notportchagre int
	   declare @NotBoatCPop int
	   declare @NotBoatCPop1 int
	   declare @NotPFPPCPop1 int
	   declare @NotForwardingCPop int
	   declare @NotPermitCPop int
	   declare @NotLevyChares int
	   declare @NotMMHECPop int
	   declare @NotAFpoCPop int
	   declare @NotSFWpoCPop int
	   declare @NotSFEWpoCPop int


		declare @CompanyRefId int 
		declare @BoatCPop1 int
		declare @SFWpoCPop int 
		declare @SFEWpoCPop int
		declare @rbtportchagdeop int
	declare @CustomerRefId int 	
	declare @EmployeeRefId int 	
	declare @UserRefId int 	
	declare @JobMasterRefId int 
	declare @AgentMasterRefId int 
	declare @AgentCompanyRefId int 
	declare @OAgentMasterRefId int 
	declare @OAgentCompanyRefId int 
	declare @QuantityList varchar(500)

	declare @SaleDate date 
	declare @SaleType varchar(20) 
	declare @BillType varchar(20) 
	
	declare @GrossAmount real 
	declare @TaxAmount real 
	declare @DiscountAmount real 
	declare @Remarks varchar(300) 
	declare @Remarks1 varchar(300) 
	declare @DODescription varchar(500) 
	
	declare @CurrencyValue real 
	declare @ActualNetAmount real 
	
	
	declare @PlusAmount real 
	declare @MinusAmount real 
	declare @Coinage real 
	declare @Amount real 
	declare @Offvesselname varchar(200) 
	declare @Loadingvesselname varchar(200) 
	declare @TruckSize varchar(200) 
	
	declare @SPort varchar(200) 
	declare @OPort varchar(200) 
	
	declare @SCN varchar(200) 
	declare @LSCN varchar(200) 
	
	declare @ETA datetime 
	declare @ETB datetime 
	declare @ETD datetime 
	declare @OETA datetime 
	declare @OETB datetime 
	declare @OETD datetime 
	declare @DOCNo int 
	declare @InvoiceNo int 
	declare @TruckRefid int 
	declare @DriverRefid int 
	declare @AWBNo varchar(100) 
	declare @BLCopy varchar(100) 
	declare @Quantity varchar(100) 
	declare @TotalWeight varchar(100) 
	declare @Vessel varchar(200) 
	declare @OVessel varchar(200) 
	
	declare @Commodity varchar(100) 
	declare @Cargo varchar(100) 
	
	
	declare @JStatus int 
	declare @OStatus int
	declare @ForkliftbyRefid int 
	declare @SealbyRefid int 
	declare @SealbreakbyRefid int 
	declare @SealbyRefid2 int 
	declare @SealbreakbyRefid2 int 
	declare @SealbyRefid3 int 
	declare @SealbreakbyRefid3 int 
	declare @BoardingOfficerRefid int 
	declare @BoardingOfficer1Refid int 
	declare @BoardingAmount real 
	declare @BoardingAmount1 real 
	declare @ForwardingEnterRef varchar(200) 
	declare @ForwardingExitRef varchar(200) 
	declare @ForwardingEnterRef2 varchar(200) 
	declare @ForwardingExitRef2 varchar(200) 
	declare @ForwardingEnterRef3 varchar(200) 
	declare @ForwardingExitRef3 varchar(200) 
	declare @ForwardingSMKNo varchar(200) 
	declare @ForwardingSMKNo2 varchar(200) 
	declare @ForwardingSMKNo3 varchar(200) 


	declare @ForwardingQuantity varchar(200)
	declare @ForwardingQuantity2 varchar(200)
	declare @ForwardingQuantity3 varchar(200)
	
	declare @PortChargesRef varchar(200) 
	declare @PortCharges real 
	declare @SealAmount real 
	declare @BreakSealAmount real 
	declare @SealAmount2 real 
	declare @BreakSealAmount2 real 
	declare @SealAmount3 real 
	declare @BreakSealAmount3 real 
	
	declare @PickupDate datetime 	
	declare @DeliveryDate datetime 
	declare @WareHouseEnterDate datetime 
	declare @WareHouseExitDate datetime 
	declare @WareHouseAddress varchar(2000) 
	declare @PickupAddress varchar(2000) 
	declare @pickuptimelist varchar (5000)
	declare @pickupQuantityList varchar(5000)
	declare @DeliveryQuantityList varchar (5000)
	declare @DelivertimeList varchar (5000)
	declare @DeliveryAddress varchar(2000) 	
	declare @Forwarding varchar(100) 
	declare @Forwarding2 varchar(100) 
	declare @Forwarding3 varchar(100) 
	declare @Origin varchar(200) 
	declare @Destination varchar(200) 	
	declare @Zb varchar(100) 
	declare @PTW varchar(100) 
	
	declare @Zb2 varchar(100) 
	declare @ZbRef varchar(200) 
	declare @ZbRef2 varchar(200) 
	declare @SaleDetails nvarchar(max)
	declare @tempid int
	declare @SaleNo int
	declare @SaleNoDisplay varchar(50)
	declare @CNumberDisplay varchar(50)
	declare @CNumber int
	declare @Completeddate datetime
	declare @Forwarding1S1 varchar(500)
	declare @Forwarding1S2 varchar(500)
	declare @Forwarding2S1 varchar(500)
	declare @Forwarding2S2 varchar(500)
	declare @Forwarding3S1 varchar(500)
	declare @Forwarding3S2 varchar(500)
	declare @trucksize2 varchar(500)
	declare @OriginRefId int
	declare @DestinationRefId int
	declare @ForwardingDate datetime
	declare @Forwarding2Date datetime
	declare @Forwarding3Date datetime
	declare @LBoardingOfficerRefid int 
	declare @LBoardingOfficer1Refid int 
	declare @LBoardingAmount real 
	declare @LBoardingAmount1 real 
	declare @LPortChargesRef varchar(200) 
	declare @LPortCharges real 
	declare @OBoardingOfficerRefid int 
	declare @OBoardingOfficer1Refid int 
	declare @OBoardingAmount real 
	declare @OBoardingAmount1 real 
	declare @OPortChargesRef varchar(200) 
	declare @OPortCharges real 
	declare @LPTW varchar(100)
	declare @OPTW varchar(100)
	declare @FlighTime datetime
	declare @SymbolRefId int
 -- Start Process
	   
	   
		   WHILE EXISTS (SELECT * FROM #temp1)
           BEGIN
		      SELECT TOP 1 
		 @tempid =tempid,
 @Id =Id ,
 @sportsaleorderid =sportsaleorderid,
 @PortCPop =PortCPop,
 @ForwardingCPop=ForwardingCPop,
 @PermitCPop =PermitCPop,
 @BoatCPop =BoatCPop,
 @LiveCPop =LiveCPop,
 @Notportchagre =Notportchagre,
 @NotBoatCPop =NotBoatCPop,
 @NotBoatCPop1 =NotBoatCPop1,
 @NotPFPPCPop1 =NotPFPPCPop1,
 @NotForwardingCPop =NotForwardingCPop,
 @NotPermitCPop =NotPermitCPop,
 @NotLevyChares =NotLevyChares,
 @NotMMHECPop =NotMMHECPop,
 @NotAFpoCPop =NotAFpoCPop,
 @NotSFWpoCPop =NotSFWpoCPop,
 @NotSFEWpoCPop =NotSFEWpoCPop,

 @MMHECPop =MMHECPop,
 @AFpoCPop =AFpoCPop,
 @BoatCPop1 =BoatCPop1,
 @SFWpoCPop =SFWpoCPop,
 @SFEWpoCPop =SFEWpoCPop,
 @rbtportchagdeop = rbtportchagdeop,
 @PFPPCPop1 =PFPPCPop1,
 @CompanyRefId=CompanyRefId,
@CustomerRefId =CustomerRefId ,
@EmployeeRefId =EmployeeRefId ,
@UserRefId =UserRefId ,
@JobMasterRefId =JobMasterRefId ,
@AgentMasterRefId=AgentMasterRefId,
@AgentCompanyRefId=AgentCompanyRefId,
@OAgentMasterRefId=OAgentMasterRefId,
@OAgentCompanyRefId=OAgentCompanyRefId,
@BillType=BillType,
@Quantitylist=Quantitylist,
@SaleDate =SaleDate ,
@SaleType =SaleType ,
@GrossAmount =GrossAmount ,
@TaxAmount =TaxAmount ,
@DiscountAmount=DiscountAmount,
@Remarks=Remarks,
@Remarks1=Remarks1,
@DODescription=DODescription,
@PlusAmount =PlusAmount ,
@MinusAmount =MinusAmount ,
@Coinage =Coinage ,
@Amount =Amount ,
@Offvesselname =Offvesselname,
@Loadingvesselname =Loadingvesselname,
@TruckSize=TruckSize,
@SPort =SPort,
@OPort=OPort,
@SCN=SCN,
@ETA =ETA ,
@ETB =ETB ,
@ETD =ETD ,
@OETA =OETA ,
@OETB =OETB ,
@OETD =OETD ,
@DOCNo =DOCNo ,
@InvoiceNo =InvoiceNo ,
@TruckRefid =TruckRefid ,
@DriverRefid =DriverRefid ,
@AWBNo =AWBNo ,
@BLCopy =BLCopy ,
@Quantity =Quantity ,
@TotalWeight =TotalWeight ,
@Vessel=Vessel,
@OVessel=OVessel,
@Commodity=Commodity,
@Cargo=Cargo,
@JStatus =JStatus ,
@OStatus =OStatus ,
@ForkliftbyRefid =ForkliftbyRefid ,
@SealbyRefid =SealbyRefid ,
@SealbreakbyRefid =SealbreakbyRefid ,
@BoardingOfficerRefid=BoardingOfficerRefid,
@BoardingOfficer1Refid=BoardingOfficer1Refid,
@BoardingAmount=BoardingAmount,
@BoardingAmount1=BoardingAmount1,
@ForwardingEnterRef=ForwardingEnterRef,
@ForwardingExitRef=ForwardingExitRef,
@PortChargesRef=PortChargesRef,
@PortCharges=PortCharges,
@SealAmount=SealAmount,
@BreakSealAmount=BreakSealAmount,
@ForwardingEnterRef2=ForwardingEnterRef2,
@ForwardingExitRef2=ForwardingExitRef2,
@ForwardingEnterRef3=ForwardingEnterRef3,
@ForwardingExitRef3=ForwardingExitRef3,
@ForwardingSMKNo=ForwardingSMKNo,
@ForwardingSMKNo2=ForwardingSMKNo2,
@ForwardingSMKNo3=ForwardingSMKNo3,
@CurrencyValue=CurrencyValue,
@ActualNetAmount=ActualNetAmount,

@Forwarding2=Forwarding2,
@Forwarding3=Forwarding3,

@ForwardingQuantity=ForwardingQuantity,
@ForwardingQuantity2=ForwardingQuantity2,
@ForwardingQuantity3=ForwardingQuantity3,

@SealAmount2=SealAmount2,
@BreakSealAmount2=BreakSealAmount2,
@SealAmount3=SealAmount3,
@BreakSealAmount3=BreakSealAmount3,
@SealbyRefid2=SealbyRefid2,
@SealbreakbyRefid2=SealbreakbyRefid2,
@SealbyRefid3=SealbyRefid3,
@SealbreakbyRefid3=SealbreakbyRefid3,
@LSCN=LSCN,
@PickupDate =PickupDate ,
@DeliveryDate =DeliveryDate ,
@WareHouseEnterDate=WareHouseEnterDate,
@WareHouseExitDate=WareHouseExitDate,
@PickupAddress =PickupAddress ,
@pickuptimelist =pickuptimelist,
@pickupQuantityList =pickupQuantityList,
@DeliveryQuantityList =DeliveryQuantityList,
@DelivertimeList=DelivertimeList,
@DeliveryAddress =DeliveryAddress ,
@WareHouseAddress=WareHouseAddress,
@Forwarding =Forwarding ,
@Origin =Origin ,
@Destination =Destination ,
@Zb =Zb ,
@PTW =PTW ,

@Zb2 =Zb2 ,
@ZbRef =ZbRef ,
@ZbRef2 =ZbRef2 ,
@CNumberDisplay = CNumberDisplay,
@CNumber = CNumber,
@Forwarding1S1 =Forwarding1S1 ,
@Forwarding1S2 =Forwarding1S2 ,
@Forwarding2S1 =Forwarding2S1 ,
@Forwarding2S2 = Forwarding2S2,
@Forwarding3S1 = Forwarding3S1,
@Forwarding3S2 =Forwarding3S2 ,
@trucksize2 = trucksize2,
@OriginRefId = OriginRefId,
@DestinationRefId =DestinationRefId ,
@ForwardingDate = ForwardingDate,
@Forwarding2Date = Forwarding2Date,
@Forwarding3Date = Forwarding3Date,
@LBoardingOfficerRefid=LBoardingOfficerRefid,
@LBoardingOfficer1Refid=LBoardingOfficer1Refid,
@LBoardingAmount=LBoardingAmount,
@LBoardingAmount1=LBoardingAmount1,
@LPortChargesRef=LPortChargesRef,
@LPortCharges=LPortCharges,
@OBoardingOfficerRefid=OBoardingOfficerRefid,
@OBoardingOfficer1Refid=OBoardingOfficer1Refid,
@OBoardingAmount=OBoardingAmount,
@OBoardingAmount1=OBoardingAmount1,
@OPortChargesRef=OPortChargesRef,
@OPortCharges=OPortCharges,
@LPTW =LPTW,
@OPTW = OPTW,
@FlighTime = FlighTime,
@SymbolRefId =SymbolRefId,
@SaleDetails =SaleDetails 

			  FROM  #temp1

--SET NULL VALUES
BEGIN
if @UserRefId=''
BEGIN
set @UserRefId=null;
END
if @EmployeeRefId=''
BEGIN
set @EmployeeRefId=null;
END
if @AgentMasterRefId=''
BEGIN
set @AgentMasterRefId=null;
END
if @AgentCompanyRefId=''
BEGIN
set @AgentCompanyRefId=null;
END
if @OAgentMasterRefId=''
BEGIN
set @OAgentMasterRefId=null;
END
if @OAgentCompanyRefId=''
BEGIN
set @OAgentCompanyRefId=null;
END
if @ETA=''
BEGIN
set @ETA=null;
END
if @ETB=''
BEGIN
set @ETB=null;
END
if @ETD=''
BEGIN
set @ETD=null;
END
if @OETA=''
BEGIN
set @OETA=null;
END
if @OETB=''
BEGIN
set @OETB=null;
END
if @OETD=''
BEGIN
set @OETD=null;
END
if @TruckRefid=''
BEGIN
set @TruckRefid=null;
END
if @DriverRefid=''
BEGIN
set @DriverRefid=null;
END
if @ForkliftbyRefid=''
BEGIN
set @ForkliftbyRefid=null;
END
if @SealbyRefid=''
BEGIN
set @SealbyRefid=null;
END
if @SealbreakbyRefid=''
BEGIN
set @SealbreakbyRefid=null;
END
if @SealbyRefid2=''
BEGIN
set @SealbyRefid2=null;
END
if @SealbreakbyRefid2=''
BEGIN
set @SealbreakbyRefid2=null;
END
if @SealbyRefid3=''
BEGIN
set @SealbyRefid3=null;
END
if @SealbreakbyRefid3=''
BEGIN
set @SealbreakbyRefid3=null;
END
if @PickupDate=''
BEGIN
set @PickupDate=null;
END
if @DeliveryDate=''
BEGIN
set @DeliveryDate=null;
END
if @WareHouseEnterDate=''
BEGIN
set @WareHouseEnterDate=null;
END
if @WareHouseExitDate=''
BEGIN
set @WareHouseExitDate=null;
END
if @BoardingOfficerRefid=''
BEGIN
set @BoardingOfficerRefid=null;
END

if @BoardingOfficer1Refid=''
BEGIN
set @BoardingOfficer1Refid=null;
END
if @LBoardingOfficerRefid=''
BEGIN
set @LBoardingOfficerRefid=null;
END

if @LBoardingOfficer1Refid=''
BEGIN
set @LBoardingOfficer1Refid=null;
END
if @OBoardingOfficerRefid=''
BEGIN
set @OBoardingOfficerRefid=null;
END

if @OBoardingOfficer1Refid=''
BEGIN
set @OBoardingOfficer1Refid=null;
END
if @trucksize2=''
BEGIN
set @trucksize2=null;
END
if @OriginRefId=''
BEGIN
set @OriginRefId=null;
END
if @DestinationRefId=''
BEGIN
set @DestinationRefId=null;
END
if @ForwardingDate = ''
BEGIN
set @ForwardingDate=null;
END
if @Forwarding2Date = ''
BEGIN
set @Forwarding2Date=null;
END
if @Forwarding3Date = ''
BEGIN
set @Forwarding3Date=null;
END
if @FlighTime = ''
BEGIN
set @FlighTime = null;
END
if @SymbolRefId = ''
BEGIN 
set @SymbolRefId =null;
END
END

	  If @UserRefId <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from AppUser with(nolock) where CompanyRefId=@Comid and Id=@UserRefId and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Login User Not Found Issue id'+@UserRefId
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @EmployeeRefId <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@EmployeeRefId and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Not Found Issue id'+@EmployeeRefId
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	  
	  If @AgentCompanyRefId <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from AgentCompanyMaster with(nolock) where CompanyRefId=@Comid and Id=@AgentCompanyRefId and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Agent Company Not Found Issue id'+@AgentCompanyRefId
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	  If @AgentMasterRefId <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from Agent with(nolock) where CompanyRefId=@Comid and Id=@AgentMasterRefId and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Agent Not Found Issue id'+@AgentMasterRefId
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	   If @OAgentCompanyRefId <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from AgentCompanyMaster with(nolock) where CompanyRefId=@Comid and Id=@OAgentCompanyRefId and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='OAgent Company Not Found Issue id'+@OAgentCompanyRefId
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	  If @OAgentMasterRefId <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from Agent with(nolock) where CompanyRefId=@Comid and Id=@OAgentMasterRefId and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='OAgent Not Found Issue id'+@OAgentMasterRefId
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  

	  If @TruckRefid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from TruckMaster with(nolock) where CompanyRefId=@Comid and Id=@TruckRefid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Truck Not Found Issue id'+@TruckRefid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @DriverRefid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from DriverMaster with(nolock) where CompanyRefId=@Comid and Id=@DriverRefid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Driver Not Found Issue id'+@DriverRefid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @ForkliftbyRefid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@ForkliftbyRefid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Forklift Not Found Issue id'+@ForkliftbyRefid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @SealbyRefid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@SealbyRefid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Sealby Not Found Issue id'+@SealbyRefid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @SealbreakbyRefid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@SealbreakbyRefid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Sealbreakby Not Found Issue id'+@SealbreakbyRefid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	   If @SealbyRefid2 <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@SealbyRefid2 and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Sealby 2 Not Found Issue id'+@SealbyRefid2
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @SealbreakbyRefid2 <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@SealbreakbyRefid2 and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Sealbreakby 2 Not Found Issue id'+@SealbreakbyRefid2
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	   If @SealbyRefid3 <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@SealbyRefid3 and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Sealby 3 Not Found Issue id'+@SealbyRefid3
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END

	  If @SealbreakbyRefid3 <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@SealbreakbyRefid3 and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee Sealbreakby 3 Not Found Issue id'+@SealbreakbyRefid3
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	   If @BoardingOfficerRefid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@BoardingOfficerRefid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee BoardingOfficerRefid Not Found Issue id'+@BoardingOfficerRefid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	   If @BoardingOfficer1Refid <> 0
	  BEGIN
	  set @countcheck = isnull((select Id from EmployeeMaster with(nolock) where CompanyRefId=@Comid and Id=@BoardingOfficer1Refid and Active=1),0)
       if(@countcheck =0)
	     BEGIN
		  set @flag =0;
		  set @msg='Employee BoardingOfficer1Refid Not Found Issue id'+@BoardingOfficer1Refid
		     ROLLBACK TRAN --RollBack in case of Error
		      SET @Result =0 
		      select @Result as Result,@msg as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
			  RETURN 
			  END
	  END
	   -- Edit Process
	   if @Id <>0 
	     BEGIN
			 delete from SaleorderDetails where SaleOrderMasterRefId=@Id 
		 END
		 if @JStatus = 8
		 begin 
		 set @Completeddate = getdate()
		 end
		 else 
		 begin 
		 set @Completeddate = null
		 end
		 --SaleOrderMaster Insert
     if @Id =0
	    BEGIN
		    insert into SaleOrderMaster([CompanyRefId] ,[sportsaleorderid],[PortCPop],[Notportchagre], [NotBoatCPop],[NotBoatCPop1], [NotPFPPCPop1], [NotForwardingCPop], [NotPermitCPop], [NotLevyChares], [NotMMHECPop], [NotAFpoCPop], [NotSFWpoCPop], [NotSFEWpoCPop],    [ForwardingCPop],[BoatCPop],[PermitCPop],[LiveCPop],[MMHECPop],[AFpoCPop],[BoatCPop1],[SFWpoCPop],[SFEWpoCPop],[PFPPCPop1],[UserRefId] ,[BillType],[EmployeeRefId],[LastEmployeeRefId] ,[rbtportchagdeop],[CustomerRefId] ,[JobMasterRefId],[AgentCompanyRefId],[AgentMasterRefId] ,[OAgentCompanyRefId],[OAgentMasterRefId] ,[SaleDate] ,[SaleType]   ,[Coinage] ,[GrossAmount] ,[TaxAmount] ,[DiscountAmount],[Remarks],[Remarks1],[DODescription],[PlusAmount] ,[MinusAmount] ,[Amount] ,[Active] ,[Created_Date] ,[Created_By] ,[Modified_Date] ,[Modified_By] ,[Offvesselname] ,[Loadingvesselname],[TruckSize] ,[SPort],[OPort] ,[SCN],[ETA] ,[ETB] ,[ETD] ,[OETA] ,[OETB] ,[OETD] ,[DOCNo] ,[InvoiceNo] ,[TruckRefid] ,[DriverRefid] ,[AWBNo] ,[BLCopy] ,[Quantity] ,[TotalWeight] ,[Vessel],[OVessel],[Commodity],[Cargo],[JStatus] ,[OStatus] ,[ForkliftbyRefid] ,[SealbyRefid] ,[SealbreakbyRefid] ,[BoardingOfficerRefid],[BoardingOfficer1Refid],[BoardingAmount],[BoardingAmount1],[ForwardingEnterRef],[ForwardingExitRef],[PortChargesRef],[PortCharges],[SealAmount],[BreakSealAmount],[ForwardingEnterRef2],[ForwardingExitRef2],[ForwardingEnterRef3],[ForwardingExitRef3],[Forwarding2],[Forwarding3],[ForwardingQuantity],[ForwardingQuantity2],[ForwardingQuantity3],[ForwardingSMKNo],[ForwardingSMKNo2],[ForwardingSMKNo3],[Zb2],[ZbRef],[ZbRef2],[SealAmount2],[BreakSealAmount2],[SealAmount3],[BreakSealAmount3],[SealbyRefid2],[SealbreakbyRefid2],[SealbyRefid3],[SealbreakbyRefid3],[LSCN],[PickupDate] ,[DeliveryDate] ,[WareHouseEnterDate],[WareHouseExitDate],[PickupAddress], [pickuptimelist],[pickupQuantityList],[DeliveryQuantityList],[DelivertimeList], [DeliveryAddress],[QuantityList] ,[WareHouseAddress],[Forwarding] ,[Origin] ,[Destination] ,[Zb],[PTW],[CNumberDisplay],[CNumber],[CurrencyValue],[ActualNetAmount],[CompletedDate],[Forwarding1S1],[Forwarding1S2],[Forwarding2S1],[Forwarding2S2],[Forwarding3S1],[Forwarding3S2],[trucksize2],[OriginRefId],[DestinationRefId],[ForwardingDate],[Forwarding2Date],[Forwarding3Date],[LBoardingOfficerRefid],[LBoardingOfficer1Refid],[LBoardingAmount],[LBoardingAmount1],[LPortChargesRef],[LPortCharges],[OBoardingOfficerRefid],[OBoardingOfficer1Refid],[OBoardingAmount],[OBoardingAmount1],[OPortChargesRef],[OPortCharges],[LPTW],[OPTW],[FlighTime],[SymbolRefId])
			                     values(@CompanyRefId,@sportsaleorderid,@PortCPop,@Notportchagre, @NotBoatCPop, @NotBoatCPop1, @NotPFPPCPop1, @NotForwardingCPop, @NotPermitCPop, @NotLevyChares, @NotMMHECPop, @NotAFpoCPop, @NotSFWpoCPop, @NotSFEWpoCPop,          @ForwardingCPop,@BoatCPop,@PermitCPop,@LiveCPop,@MMHECPop,@AFpoCPop,@BoatCPop1,@SFWpoCPop,@SFEWpoCPop,@PFPPCPop1, @UserRefId,@BillType,@EmployeeRefId,@EmployeeRefId,@rbtportchagdeop,@CustomerRefId,@JobMasterRefId,@AgentCompanyRefId,@AgentMasterRefId,@OAgentCompanyRefId,@OAgentMasterRefId,@SaleDate,@SaleType,@Coinage,@GrossAmount,@TaxAmount,@DiscountAmount,@Remarks,@Remarks1,@DODescription,@PlusAmount,@MinusAmount,@Amount,1,getdate(),(suser_name()),getdate(),(suser_name()),@Offvesselname,@Loadingvesselname,@TruckSize,@SPort,@OPort,@SCN,@ETA,@ETB,@ETD,@OETA,@OETB,@OETD,@DOCNo,@InvoiceNo,@TruckRefid,@DriverRefid,@AWBNo,@BLCopy,@Quantity,@TotalWeight,@Vessel,@OVessel,@Commodity,@Cargo,@JStatus,@OStatus,@ForkliftbyRefid,@SealbyRefid,@SealbreakbyRefid,@BoardingOfficerRefid,@BoardingOfficer1Refid,@BoardingAmount,@BoardingAmount1,@ForwardingEnterRef,@ForwardingExitRef,@PortChargesRef,@PortCharges,@SealAmount,@BreakSealAmount,@ForwardingEnterRef2,@ForwardingExitRef2,@ForwardingEnterRef3,@ForwardingExitRef3,@Forwarding2,@Forwarding3,@ForwardingQuantity,@ForwardingQuantity2,@ForwardingQuantity3,@ForwardingSMKNo,@ForwardingSMKNo2,@ForwardingSMKNo3,@Zb2,@ZbRef,@ZbRef2,@SealAmount2,@BreakSealAmount2,@SealAmount3,@BreakSealAmount3,@SealbyRefid2,@SealbreakbyRefid2,@SealbyRefid3,@SealbreakbyRefid3,@LSCN,@PickupDate,@DeliveryDate,@WareHouseEnterDate,@WareHouseExitDate,@PickupAddress,@pickuptimelist,@pickupQuantityList,@DeliveryQuantityList,@DelivertimeList, @DeliveryAddress,@Quantitylist,@WareHouseAddress,@Forwarding,@Origin,@Destination,@Zb,@PTW,@CNumberDisplay,@CNumber,@CurrencyValue,@ActualNetAmount,@Completeddate,@Forwarding1S1,@Forwarding1S2,@Forwarding2S1,@Forwarding2S2,@Forwarding3S1,@Forwarding3S2,@trucksize2,@OriginRefId,@DestinationRefId,@ForwardingDate,@Forwarding2Date,@Forwarding3Date,@LBoardingOfficerRefid,@LBoardingOfficer1Refid,@LBoardingAmount,@LBoardingAmount1,@LPortChargesRef,@LPortCharges,@OBoardingOfficerRefid,@OBoardingOfficer1Refid,@OBoardingAmount,@OBoardingAmount1,@OPortChargesRef,@OPortCharges,@LPTW,@OPTW,@FlighTime,@SymbolRefId)
		set @saleid = (select Scope_identity() AS IdNew)
		END  
    else
	   BEGIN
	     set  @saleid = @Id


	       Update SaleOrderMaster
		   set 
CustomerRefId =@CustomerRefId ,
sportsaleorderid=@sportsaleorderid,
PortCPop =@PortCPop,
NotBoatCPop=@NotBoatCPop,
NotBoatCPop1=@NotBoatCPop1,
NotPFPPCPop1=@NotPFPPCPop1,
NotForwardingCPop=@NotForwardingCPop,
NotPermitCPop=@NotPermitCPop,
NotLevyChares=@NotLevyChares,
NotMMHECPop=@NotMMHECPop,
NotAFpoCPop=@NotAFpoCPop,
NotSFWpoCPop=@NotSFWpoCPop,
NotSFEWpoCPop=@NotSFEWpoCPop,
Notportchagre=@Notportchagre,



BoatCPop=@BoatCPop,
LiveCPop=@LiveCPop,
MMHECPop=@MMHECPop,
AFpoCPop=@AFpoCPop,
BoatCPop1=@BoatCPop1,
SFWpoCPop=@SFWpoCPop,
SFEWpoCPop=@SFEWpoCPop,
PFPPCPop1=@PFPPCPop1,
rbtportchagdeop =@rbtportchagdeop,
ForwardingCPop=@ForwardingCPop,
PermitCPop=@PermitCPop,
Quantitylist=@Quantitylist,
LastEmployeeRefId =@EmployeeRefId ,
UserRefId =@UserRefId ,
JobMasterRefId =@JobMasterRefId ,
AgentCompanyRefId=@AgentCompanyRefId,
AgentMasterRefId=@AgentMasterRefId,
OAgentCompanyRefId=@OAgentCompanyRefId,
OAgentMasterRefId=@OAgentMasterRefId,
SaleDate =@SaleDate ,
SaleType =@SaleType ,
GrossAmount =@GrossAmount ,
TaxAmount =@TaxAmount ,
DiscountAmount=@DiscountAmount,
Remarks=@Remarks,
Remarks1=@Remarks1,
DODescription=@DODescription,
PlusAmount =@PlusAmount ,
MinusAmount =@MinusAmount ,
Coinage =@Coinage ,
Amount =@Amount ,
Offvesselname =@Offvesselname ,
Loadingvesselname =@Loadingvesselname ,
TruckSize=@TruckSize,
SPort =@SPort ,
OPort=@OPort,
SCN=@SCN,
ETA =@ETA ,
ETB =@ETB ,
ETD =@ETD ,
OETA =@OETA ,
OETB =@OETB ,
OETD =@OETD ,
--DOCNo =@DOCNo ,
--InvoiceNo =@InvoiceNo ,
TruckRefid =@TruckRefid ,
DriverRefid =@DriverRefid ,
AWBNo =@AWBNo ,
BLCopy =@BLCopy ,
Quantity =@Quantity ,
TotalWeight =@TotalWeight ,
Vessel=@Vessel,
OVessel=@OVessel,
Commodity=@Commodity,
Cargo=@Cargo,
JStatus =@JStatus ,
OStatus =@OStatus ,
ForkliftbyRefid =@ForkliftbyRefid ,
SealbyRefid =@SealbyRefid ,
SealbreakbyRefid =@SealbreakbyRefid ,
BoardingOfficerRefid=@BoardingOfficerRefid,
BoardingOfficer1Refid=@BoardingOfficer1Refid,
BoardingAmount=@BoardingAmount,
BoardingAmount1=@BoardingAmount1,
ForwardingEnterRef=@ForwardingEnterRef,
ForwardingExitRef=@ForwardingExitRef,
PortChargesRef=@PortChargesRef,
PortCharges=@PortCharges,
SealAmount=@SealAmount,
BreakSealAmount=@BreakSealAmount,
ForwardingEnterRef2=@ForwardingEnterRef2,
ForwardingExitRef2=@ForwardingExitRef2,
ForwardingEnterRef3=@ForwardingEnterRef3,
ForwardingExitRef3=@ForwardingExitRef3,
Forwarding2=@Forwarding2,
Forwarding3=@Forwarding3,
ForwardingSMKNo=@ForwardingSMKNo,
ForwardingSMKNo2=@ForwardingSMKNo2,
ForwardingSMKNo3=@ForwardingSMKNo3,

ForwardingQuantity=@ForwardingQuantity,
ForwardingQuantity2=@ForwardingQuantity2,
ForwardingQuantity3 =@ForwardingQuantity3 ,

Zb2=@Zb2,
ZbRef=@ZbRef,
ZbRef2=@ZbRef2,
SealAmount2=@SealAmount2,
BreakSealAmount2=@BreakSealAmount2,
SealAmount3=@SealAmount3,
BreakSealAmount3=@BreakSealAmount3,
SealbyRefid2=@SealbyRefid2,
SealbreakbyRefid2=@SealbreakbyRefid2,
SealbyRefid3=@SealbyRefid3,
SealbreakbyRefid3=@SealbreakbyRefid3,
LSCN=@LSCN,
PickupDate =@PickupDate,
DeliveryDate =@DeliveryDate,
WareHouseEnterDate=@WareHouseEnterDate,
WareHouseExitDate=@WareHouseExitDate,
WareHouseAddress=@WareHouseAddress,
PickupAddress =@PickupAddress,
pickuptimelist=@pickuptimelist,
pickupQuantityList=@pickupQuantityList,
DeliveryQuantityList=@DeliveryQuantityList,
DelivertimeList=@DelivertimeList,
DeliveryAddress =@DeliveryAddress ,
Forwarding =@Forwarding ,
Origin =@Origin ,
Destination =@Destination ,
Zb =@Zb,
PTW=@PTW,
CurrencyValue=@CurrencyValue,
ActualNetAmount=@ActualNetAmount,
CompletedDate= @Completeddate,
Forwarding1S1 =@Forwarding1S1 ,
Forwarding1S2 =@Forwarding1S2,
Forwarding2S1=@Forwarding2S1,
Forwarding2S2=@Forwarding2S2,
Forwarding3S1=@Forwarding3S1,
Forwarding3S2= @Forwarding3S2,
trucksize2=@trucksize2,
OriginRefId=@OriginRefId,
DestinationRefId= @DestinationRefId,
ForwardingDate = @ForwardingDate,
Forwarding2Date = @Forwarding2Date,
Forwarding3Date = @Forwarding3Date,
LBoardingOfficerRefid=@LBoardingOfficerRefid,
LBoardingOfficer1Refid=@LBoardingOfficer1Refid,
LBoardingAmount=@LBoardingAmount,
LBoardingAmount1=@LBoardingAmount1,
LPortChargesRef=@LPortChargesRef,
LPortCharges=@LPortCharges,
OBoardingOfficerRefid=@OBoardingOfficerRefid,
OBoardingOfficer1Refid=@OBoardingOfficer1Refid,
OBoardingAmount=@OBoardingAmount,
OBoardingAmount1=@OBoardingAmount1,
OPortChargesRef=@OPortChargesRef,
OPortCharges=@OPortCharges,
LPTW=@LPTW,
OPTW=@OPTW,
FlighTime=@FlighTime,
SymbolRefId=@SymbolRefId
--CNumberDisplay=@CNumberDisplay,
--CNumber=@CNumber

where id=@id
	   END

      -- SaleDetails Insert

		 INSERT INTO SaleOrderDetails (SaleOrderMasterRefId ,ItemMasterRefId,SDRemarks ,MRP ,PurchaseRate ,ItemQty ,DiscPer ,DiscAmount ,LandingCost ,TaxPercent ,TaxAmount ,SalesRate ,TaxRefId,NetSalesRate ,Amount ,Created_Date ,Modified_Date,CurrencyValue,ActualAmount)
         SELECT @saleid as SaleOrderMasterRefId,ItemMasterRefId,SDRemarks,MRP,PurchaseRate,ItemQty,DiscPer,DiscAmount
		 ,LandingCost,TaxPercent,TaxAmount,SalesRate,TaxRefId,NetSalesRate,Amount,GETDATE(),GETDATE(),CurrencyValue,ActualAmount
    FROM OPENJSON(@SaleDetails)
    WITH (	   
	ItemMasterRefId int '$.ItemMasterRefId',    
	SDRemarks varchar(300) '$.SDRemarks',    
	
	MRP real '$.MRP',   
    PurchaseRate real '$.PurchaseRate',   
	ItemQty real '$.ItemQty',   
	DiscPer real '$.DiscPer',   


	DiscAmount real '$.DiscAmount',   
	LandingCost real '$.LandingCost',   
	TaxPercent real '$.TaxPercent',   
	TaxAmount real'$.TaxAmount',    
	SalesRate real '$.SalesRate', 
	TaxRefId int '$.TaxRefId', 
	NetSalesRate real '$.NetSalesRate',  
	Amount real '$.Amount',
	CurrencyValue real '$.CurrencyValue',
	ActualAmount real '$.ActualAmount')
 IF @id =0  
     BEGIN    
        
      
				   SET @count = (SELECT ISNULL(MAX(SequenceNo),0) FROM  SequenceNoMaster  Where CompanyRefId = @CompanyRefId and SequenceName='SaleOrderMaster'+@BillType)     
				       IF @count =0       
					         BEGIN          
					       
										     update SaleOrderMaster set CNumber = 1 where Id = @saleid       
											       SET @SaleNo =1     
												   update SequenceNoMaster set SequenceNo=@SaleNo Where CompanyRefId = @CompanyRefId and SequenceName='SaleOrderMaster'+@BillType  
									  END
										  Else       
										     BEGIN           
											     SET @SaleNo = (SELECT ISNULL(MAX(SequenceNo)+1,1) FROM  SequenceNoMaster  Where CompanyRefId = @CompanyRefId and SequenceName='SaleOrderMaster'+@BillType)
												 update SaleOrderMaster set CNumber = @SaleNo where Id = @saleid       								
			                                     update SequenceNoMaster set SequenceNo=@SaleNo Where CompanyRefId = @CompanyRefId and SequenceName='SaleOrderMaster'+@BillType
											 END              


	    
					SET @SaleNoDisplay =  @BillType+RIGHT('000000000'+cast(@SaleNo as varchar(50))  ,9)       
			
	     update SaleOrderMaster set CNumberDisplay = @SaleNoDisplay where Id = @saleid  
 END 
   --DECLARE @resultdata1 int
 	 -- EXEC @resultdata1 = sp_releaseapplock @Resource = 'Form1'
      delete from #temp1 where tempid=@tempid
 END
	COMMIT TRAN -- Transaction Success!
	SET @Result =1;
	select @Result as Result,@msg as msg,@SaleNoDisplay AS BillNo,GETDATE() as SaleTime,@saleid as id;

END TRY
 BEGIN CATCH		
		IF @@TRANCOUNT > 0

		DECLARE @Message varchar(MAX) = ERROR_MESSAGE();
			ROLLBACK TRAN --RollBack in case of Error
		SET @Result =0 
		select @Result as Result,@Message as msg,'' AS BillNo,'' AS BillNo,GETDATE() as SaleTime,@saleid as id;
		select @Message as Massge
 END CATCH
END
GO

