USE [LiveMaleva2]
GO

/****** Object:  Table [dbo].[PLANINGDetails]    Script Date: 15-02-2026 11.48.35 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[PLANINGDetails](
	[Id] [int] IDENTITY(1,1) NOT FOR REPLICATION NOT NULL,
	[PLANINGMasterRefId] [int] NOT NULL,
	[SaleOrderMasterRefId] [int] NOT NULL,
	[TruckRefid] [int] NULL,
	[Remarks] [varchar](300) NULL,
	[Created_Date] [datetime] NOT NULL,
	[Modified_Date] [datetime] NOT NULL,
	[OriginD] [varchar](150) NULL,
	[DestinationD] [varchar](150) NULL,
	[PickupDateD] [datetime] NULL,
	[DeliveryDateD] [datetime] NULL,
	[SortBy] [int] NOT NULL,
	[TruckNameD] [varchar](200) NULL,
	[DriverNameD] [varchar](200) NULL,
	[pickuptimelist] [varchar](500) NULL,
	[pickupQuantitylist] [varchar](500) NULL,
	[DeliveryQuantitylist] [varchar](500) NULL,
	[Delivertimelist] [varchar](500) NULL,
 CONSTRAINT [PK_PLANINGDetails] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[PLANINGDetails] ADD  CONSTRAINT [DF_PLANINGDetails_Created_Date]  DEFAULT (getdate()) FOR [Created_Date]
GO

ALTER TABLE [dbo].[PLANINGDetails] ADD  CONSTRAINT [DF_PLANINGDetails_Modified_Date]  DEFAULT (getdate()) FOR [Modified_Date]
GO

ALTER TABLE [dbo].[PLANINGDetails] ADD  DEFAULT ((0)) FOR [SortBy]
GO

ALTER TABLE [dbo].[PLANINGDetails]  WITH NOCHECK ADD  CONSTRAINT [FK_PLANINGDetails_PLANINGMaster] FOREIGN KEY([PLANINGMasterRefId])
REFERENCES [dbo].[PLANINGMaster] ([Id])
ON UPDATE CASCADE
ON DELETE CASCADE
NOT FOR REPLICATION 
GO

ALTER TABLE [dbo].[PLANINGDetails] CHECK CONSTRAINT [FK_PLANINGDetails_PLANINGMaster]
GO

ALTER TABLE [dbo].[PLANINGDetails]  WITH NOCHECK ADD  CONSTRAINT [FK_PLANINGDetails_SaleOrderMaster] FOREIGN KEY([SaleOrderMasterRefId])
REFERENCES [dbo].[SaleOrderMaster] ([Id])
GO

ALTER TABLE [dbo].[PLANINGDetails] CHECK CONSTRAINT [FK_PLANINGDetails_SaleOrderMaster]
GO

ALTER TABLE [dbo].[PLANINGDetails]  WITH NOCHECK ADD  CONSTRAINT [FK_PLANINGDetails_TruckMaster] FOREIGN KEY([TruckRefid])
REFERENCES [dbo].[TruckMaster] ([Id])
GO

ALTER TABLE [dbo].[PLANINGDetails] CHECK CONSTRAINT [FK_PLANINGDetails_TruckMaster]
GO

