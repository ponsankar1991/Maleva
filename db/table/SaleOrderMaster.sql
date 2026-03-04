USE [LiveMaleva2]
GO

/****** Object:  Table [dbo].[SaleOrderMaster]    Script Date: 3/3/2026 4:23:24 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[SaleOrderMaster](
	[Id] [int] IDENTITY(1,1) NOT FOR REPLICATION NOT NULL,
	[CompanyRefId] [int] NOT NULL,
	[UserRefId] [int] NULL,
	[EmployeeRefId] [int] NULL,
	[CustomerRefId] [int] NOT NULL,
	[JobMasterRefId] [int] NOT NULL,
	[AgentCompanyRefId] [int] NULL,
	[AgentMasterRefId] [int] NULL,
	[SaleDate] [datetime] NOT NULL,
	[BillType] [varchar](50) NOT NULL,
	[SaleType] [varchar](50) NOT NULL,
	[CNumberDisplay] [varchar](300) NOT NULL,
	[CNumber] [int] NOT NULL,
	[Coinage] [real] NOT NULL,
	[GrossAmount] [real] NOT NULL,
	[TaxAmount] [real] NOT NULL,
	[DiscountAmount] [real] NOT NULL,
	[PlusAmount] [real] NOT NULL,
	[MinusAmount] [real] NOT NULL,
	[Amount] [real] NOT NULL,
	[Remarks] [varchar](300) NULL,
	[Active] [int] NOT NULL,
	[Created_Date] [datetime] NOT NULL,
	[Created_By] [varchar](50) NOT NULL,
	[Modified_Date] [datetime] NOT NULL,
	[Modified_By] [varchar](50) NOT NULL,
	[Offvesselname] [varchar](200) NULL,
	[Loadingvesselname] [varchar](200) NULL,
	[SPort] [varchar](200) NULL,
	[Vessel] [varchar](200) NULL,
	[Commodity] [varchar](200) NULL,
	[ETA] [datetime] NULL,
	[ETB] [datetime] NULL,
	[ETD] [datetime] NULL,
	[DOCNo] [int] NULL,
	[InvoiceNo] [int] NULL,
	[TruckRefid] [int] NULL,
	[DriverRefid] [int] NULL,
	[AWBNo] [varchar](100) NULL,
	[BLCopy] [varchar](100) NULL,
	[Quantity] [varchar](100) NULL,
	[TotalWeight] [varchar](100) NULL,
	[JStatus] [int] NULL,
	[OStatus] [int] NULL,
	[ForkliftbyRefid] [int] NULL,
	[SealbyRefid] [int] NULL,
	[SealbreakbyRefid] [int] NULL,
	[PickupDate] [datetime] NULL,
	[DeliveryDate] [datetime] NULL,
	[PickupAddress] [varchar](2000) NULL,
	[DeliveryAddress] [varchar](2000) NULL,
	[Forwarding] [varchar](50) NULL,
	[Origin] [varchar](200) NULL,
	[Destination] [varchar](200) NULL,
	[Zb] [varchar](50) NULL,
	[OETA] [datetime] NULL,
	[OETB] [datetime] NULL,
	[OETD] [datetime] NULL,
	[OAgentCompanyRefId] [int] NULL,
	[OAgentMasterRefId] [int] NULL,
	[DODescription] [varchar](500) NULL,
	[SCN] [varchar](200) NULL,
	[TruckSize] [varchar](200) NULL,
	[LastEmployeeRefId] [int] NULL,
	[WareHouseEnterDate] [datetime] NULL,
	[WareHouseExitDate] [datetime] NULL,
	[WareHouseAddress] [varchar](2000) NULL,
	[BoardingOfficerRefid] [int] NULL,
	[BoardingOfficer1Refid] [int] NULL,
	[BoardingAmount] [real] NOT NULL,
	[BoardingAmount1] [real] NOT NULL,
	[ForwardingEnterRef] [varchar](200) NULL,
	[ForwardingExitRef] [varchar](200) NULL,
	[PortChargesRef] [varchar](200) NULL,
	[PortCharges] [real] NOT NULL,
	[SealAmount] [real] NOT NULL,
	[BreakSealAmount] [real] NOT NULL,
	[ForwardingEnterRef2] [varchar](200) NULL,
	[ForwardingExitRef2] [varchar](200) NULL,
	[ForwardingEnterRef3] [varchar](200) NULL,
	[ForwardingExitRef3] [varchar](200) NULL,
	[Forwarding2] [varchar](50) NULL,
	[Forwarding3] [varchar](50) NULL,
	[Zb2] [varchar](50) NULL,
	[ZbRef] [varchar](200) NULL,
	[ZbRef2] [varchar](200) NULL,
	[SealAmount2] [real] NOT NULL,
	[BreakSealAmount2] [real] NOT NULL,
	[SealAmount3] [real] NOT NULL,
	[BreakSealAmount3] [real] NOT NULL,
	[SealbyRefid2] [int] NULL,
	[SealbreakbyRefid2] [int] NULL,
	[SealbyRefid3] [int] NULL,
	[SealbreakbyRefid3] [int] NULL,
	[LSCN] [varchar](200) NULL,
	[Cargo] [varchar](200) NULL,
	[PTW] [varchar](100) NULL,
	[OVessel] [varchar](200) NULL,
	[OPort] [varchar](200) NULL,
	[BoardingStartTime] [datetime] NULL,
	[BoardingEndTime] [datetime] NULL,
	[DriverStatus] [varchar](50) NULL,
	[ForwardingSMKNo] [varchar](200) NULL,
	[ForwardingSMKNo2] [varchar](200) NULL,
	[ForwardingSMKNo3] [varchar](200) NULL,
	[CurrencyValue] [real] NULL,
	[ActualNetAmount] [real] NULL,
	[Remarks1] [varchar](300) NULL,
	[CompletedDate] [datetime] NULL,
	[Forwarding1S1] [varchar](500) NULL,
	[Forwarding1S2] [varchar](500) NULL,
	[Forwarding2S1] [varchar](500) NULL,
	[Forwarding2S2] [varchar](500) NULL,
	[Forwarding3S1] [varchar](500) NULL,
	[Forwarding3S2] [varchar](500) NULL,
	[trucksize2] [varchar](500) NULL,
	[OriginRefId] [int] NULL,
	[DestinationRefId] [int] NULL,
	[ForwardingDate] [datetime] NULL,
	[Forwarding2Date] [datetime] NULL,
	[Forwarding3Date] [datetime] NULL,
	[TruckName1] [varchar](250) NULL,
	[RemarkDetails] [varchar](500) NULL,
	[DriverName] [varchar](150) NULL,
	[LBoardingOfficerRefid] [int] NULL,
	[LBoardingOfficer1Refid] [int] NULL,
	[LBoardingAmount] [real] NOT NULL,
	[LBoardingAmount1] [real] NOT NULL,
	[LPortChargesRef] [varchar](200) NULL,
	[LPortCharges] [real] NOT NULL,
	[OBoardingOfficerRefid] [int] NULL,
	[OBoardingOfficer1Refid] [int] NULL,
	[OBoardingAmount] [real] NOT NULL,
	[OBoardingAmount1] [real] NOT NULL,
	[OPortChargesRef] [varchar](200) NULL,
	[OPortCharges] [real] NOT NULL,
	[LPTW] [varchar](100) NULL,
	[OPTW] [varchar](100) NULL,
	[FlighTime] [datetime] NULL,
	[SymbolRefId] [int] NULL,
	[ForwardingQuantity] [varchar](200) NULL,
	[ForwardingQuantity2] [varchar](200) NULL,
	[ForwardingQuantity3] [varchar](200) NULL,
	[PortCPop] [int] NULL,
	[ForwardingCPop] [int] NULL,
	[BoatCPop] [int] NULL,
	[PermitCPop] [int] NULL,
	[QuantityList] [varchar](500) NULL,
	[Livecpop] [int] NULL,
	[MMHECPop] [int] NULL,
	[AFpoCPop] [int] NULL,
	[PPFpoCPop] [int] NULL,
	[SFEWpoCPop] [int] NULL,
	[SFWpoCPop] [int] NULL,
	[BoatCPop1] [int] NULL,
	[PFPPCPop1] [int] NULL,
	[rbtportchagdeop] [varchar](50) NULL,
	[pickuptimelist] [varchar](5000) NULL,
	[pickupQuantitylist] [varchar](5000) NULL,
	[DeliveryQuantitylist] [varchar](5000) NULL,
	[Delivertimelist] [varchar](5000) NULL,
	[Notportchagre] [int] NULL,
	[NotBoatCPop] [int] NULL,
	[NotBoatCPop1] [int] NULL,
	[NotPFPPCPop1] [int] NULL,
	[NotForwardingCPop] [int] NULL,
	[NotPermitCPop] [int] NULL,
	[NotLevyChares] [int] NULL,
	[NotMMHECPop] [int] NULL,
	[NotAFpoCPop] [int] NULL,
	[NotSFWpoCPop] [int] NULL,
	[NotSFEWpoCPop] [int] NULL,
	[OIDateIn] [varchar](500) NULL,
	[ODIDateOut] [varchar](500) NULL,
	[sportsaleorderid] [int] NULL,
 CONSTRAINT [PK_SaleOrderMaster] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Coinage]  DEFAULT ((0)) FOR [Coinage]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_GrossAmount]  DEFAULT ((0)) FOR [GrossAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_TaxAmount]  DEFAULT ((0)) FOR [TaxAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_DiscountAmount]  DEFAULT ((0)) FOR [DiscountAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_PlusAmount]  DEFAULT ((0)) FOR [PlusAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_MinusAmount]  DEFAULT ((0)) FOR [MinusAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Amount]  DEFAULT ((0)) FOR [Amount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Active]  DEFAULT ((0)) FOR [Active]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Created_Date]  DEFAULT (getdate()) FOR [Created_Date]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Created_By]  DEFAULT (suser_name()) FOR [Created_By]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Modified_Date]  DEFAULT (getdate()) FOR [Modified_Date]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_Modified_By]  DEFAULT (suser_name()) FOR [Modified_By]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_BoardingAmount]  DEFAULT ((0)) FOR [BoardingAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_BoardingAmount1]  DEFAULT ((0)) FOR [BoardingAmount1]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_PortCharges]  DEFAULT ((0)) FOR [PortCharges]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_SealAmount]  DEFAULT ((0)) FOR [SealAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_BreakSealAmount]  DEFAULT ((0)) FOR [BreakSealAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_SealAmount2]  DEFAULT ((0)) FOR [SealAmount2]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_BreakSealAmount2]  DEFAULT ((0)) FOR [BreakSealAmount2]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_SealAmount3]  DEFAULT ((0)) FOR [SealAmount3]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [DF_SaleOrderMaster_BreakSealAmount3]  DEFAULT ((0)) FOR [BreakSealAmount3]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [FK_SaleOrderMaster_CurrencyValue]  DEFAULT ((0)) FOR [CurrencyValue]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  CONSTRAINT [FK_SaleOrderMaster_ActualNetAmount]  DEFAULT ((0)) FOR [ActualNetAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [LBoardingAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [LBoardingAmount1]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [LPortCharges]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [OBoardingAmount]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [OBoardingAmount1]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [OPortCharges]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [Notportchagre]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotBoatCPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotBoatCPop1]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotPFPPCPop1]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotForwardingCPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotPermitCPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotLevyChares]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotMMHECPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotAFpoCPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotSFWpoCPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [NotSFEWpoCPop]
GO

ALTER TABLE [dbo].[SaleOrderMaster] ADD  DEFAULT ((0)) FOR [sportsaleorderid]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_Agent] FOREIGN KEY([AgentMasterRefId])
REFERENCES [dbo].[Agent] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_Agent]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_Appuser] FOREIGN KEY([UserRefId])
REFERENCES [dbo].[AppUser] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_Appuser]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_Company] FOREIGN KEY([CompanyRefId])
REFERENCES [dbo].[Company] ([Id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_Company]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_Customer] FOREIGN KEY([CustomerRefId])
REFERENCES [dbo].[Customer] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_Customer]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_DriverMaster] FOREIGN KEY([DriverRefid])
REFERENCES [dbo].[DriverMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_DriverMaster]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster] FOREIGN KEY([EmployeeRefId])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster1] FOREIGN KEY([ForkliftbyRefid])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster1]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster2] FOREIGN KEY([SealbyRefid])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster2]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster3] FOREIGN KEY([SealbreakbyRefid])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster3]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster4] FOREIGN KEY([BoardingOfficerRefid])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster4]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster5] FOREIGN KEY([BoardingOfficer1Refid])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster5]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster6] FOREIGN KEY([SealbyRefid2])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster6]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster7] FOREIGN KEY([SealbreakbyRefid2])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster7]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster8] FOREIGN KEY([SealbyRefid3])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster8]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster9] FOREIGN KEY([SealbreakbyRefid3])
REFERENCES [dbo].[EmployeeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_EmployeeMaster9]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_JobTypeMaster] FOREIGN KEY([JobMasterRefId])
REFERENCES [dbo].[JobTypeMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_JobTypeMaster]
GO

ALTER TABLE [dbo].[SaleOrderMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleOrderMaster_TruckMaster] FOREIGN KEY([TruckRefid])
REFERENCES [dbo].[TruckMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleOrderMaster] CHECK CONSTRAINT [FK_SaleOrderMaster_TruckMaster]
GO

