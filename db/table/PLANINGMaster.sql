USE [LiveMaleva2]
GO

/****** Object:  Table [dbo].[PLANINGMaster]    Script Date: 15-02-2026 11.50.46 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[PLANINGMaster](
	[Id] [int] IDENTITY(1,1) NOT FOR REPLICATION NOT NULL,
	[CompanyRefId] [int] NOT NULL,
	[UserRefId] [int] NULL,
	[EmployeeRefId] [int] NULL,
	[SaleDate] [datetime] NOT NULL,
	[FDate] [datetime] NOT NULL,
	[TDate] [datetime] NOT NULL,
	[CNumberDisplay] [varchar](300) NOT NULL,
	[CNumber] [int] NOT NULL,
	[Remarks] [varchar](2000) NULL,
	[Search] [varchar](2000) NULL,
	[Active] [int] NOT NULL,
	[Created_Date] [datetime] NOT NULL,
	[Created_By] [varchar](50) NOT NULL,
	[Modified_Date] [datetime] NOT NULL,
	[Modified_By] [varchar](50) NOT NULL,
	[LastEmployeeRefId] [int] NULL,
 CONSTRAINT [PK_PLANINGMaster] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[PLANINGMaster] ADD  CONSTRAINT [DF_PLANINGMaster_Active]  DEFAULT ((0)) FOR [Active]
GO

ALTER TABLE [dbo].[PLANINGMaster] ADD  CONSTRAINT [DF_PLANINGMaster_Created_Date]  DEFAULT (getdate()) FOR [Created_Date]
GO

ALTER TABLE [dbo].[PLANINGMaster] ADD  CONSTRAINT [DF_PLANINGMaster_Created_By]  DEFAULT (suser_name()) FOR [Created_By]
GO

ALTER TABLE [dbo].[PLANINGMaster] ADD  CONSTRAINT [DF_PLANINGMaster_Modified_Date]  DEFAULT (getdate()) FOR [Modified_Date]
GO

ALTER TABLE [dbo].[PLANINGMaster] ADD  CONSTRAINT [DF_PLANINGMaster_Modified_By]  DEFAULT (suser_name()) FOR [Modified_By]
GO

