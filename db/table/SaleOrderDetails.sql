USE [LiveMaleva2]
GO

/****** Object:  Table [dbo].[SaleDetails]    Script Date: 3/3/2026 12:07:42 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[SaleDetails](
	[Id] [int] IDENTITY(1,1) NOT FOR REPLICATION NOT NULL,
	[SaleMasterRefId] [int] NOT NULL,
	[ItemMasterRefId] [int] NOT NULL,
	[MRP] [real] NOT NULL,
	[PurchaseRate] [real] NOT NULL,
	[ItemQty] [real] NOT NULL,
	[DiscPer] [real] NOT NULL,
	[DiscAmount] [real] NOT NULL,
	[LandingCost] [real] NOT NULL,
	[TaxPercent] [real] NOT NULL,
	[TaxAmount] [real] NOT NULL,
	[SalesRate] [real] NOT NULL,
	[NetSalesRate] [real] NOT NULL,
	[Amount] [real] NOT NULL,
	[Created_Date] [datetime] NOT NULL,
	[Modified_Date] [datetime] NOT NULL,
	[CurrencyValue] [real] NULL,
	[ActualAmount] [real] NULL,
	[SDRemarks] [varchar](300) NULL,
	[SaleOrderMasterRefId] [int] NULL,
	[TaxRefId] [int] NULL,
 CONSTRAINT [PK_SaleDetails] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_MRP]  DEFAULT ((0)) FOR [MRP]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_PurchaseRate]  DEFAULT ((0)) FOR [PurchaseRate]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_ItemQty]  DEFAULT ((0)) FOR [ItemQty]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_DiscAmount]  DEFAULT ((0)) FOR [DiscAmount]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_LandingCost]  DEFAULT ((0)) FOR [LandingCost]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_TaxPercent]  DEFAULT ((0)) FOR [TaxPercent]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_TaxAmount]  DEFAULT ((0)) FOR [TaxAmount]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_SalesRate]  DEFAULT ((0)) FOR [SalesRate]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_NetSalesRate]  DEFAULT ((0)) FOR [NetSalesRate]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_Amount]  DEFAULT ((0)) FOR [Amount]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [DF_SaleDetails_Created_Date]  DEFAULT (getdate()) FOR [Created_Date]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [DF_SaleDetails_Modified_Date]  DEFAULT (getdate()) FOR [Modified_Date]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_CurrencyValue]  DEFAULT ((0)) FOR [CurrencyValue]
GO

ALTER TABLE [dbo].[SaleDetails] ADD  CONSTRAINT [FK_SaleDetails_ActualAmount]  DEFAULT ((0)) FOR [ActualAmount]
GO

ALTER TABLE [dbo].[SaleDetails]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleDetails_ItemMaster] FOREIGN KEY([ItemMasterRefId])
REFERENCES [dbo].[ItemMaster] ([Id])
GO

ALTER TABLE [dbo].[SaleDetails] CHECK CONSTRAINT [FK_SaleDetails_ItemMaster]
GO

ALTER TABLE [dbo].[SaleDetails]  WITH NOCHECK ADD  CONSTRAINT [FK_SaleDetails_SaleMaster] FOREIGN KEY([SaleMasterRefId])
REFERENCES [dbo].[SaleMaster] ([Id])
ON UPDATE CASCADE
ON DELETE CASCADE
NOT FOR REPLICATION 
GO

ALTER TABLE [dbo].[SaleDetails] CHECK CONSTRAINT [FK_SaleDetails_SaleMaster]
GO

