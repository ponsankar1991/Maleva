USE [LiveMaleva2]
GO

/****** Object:  Table [dbo].[PortMaster]    Script Date: 15-02-2026 12.32.28 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[PortMaster](
	[Id] [int] IDENTITY(1,1) NOT FOR REPLICATION NOT NULL,
	[CompanyRefId] [int] NOT NULL,
	[PortName] [varchar](50) NOT NULL,
	[Created_Date] [datetime] NOT NULL,
	[Modified_Date] [datetime] NOT NULL,
	[Modified_By] [varchar](50) NOT NULL,
	[Active] [int] NOT NULL,
 CONSTRAINT [PK_PortMaster] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[PortMaster] ADD  CONSTRAINT [DF_PortMaster_Created_Date]  DEFAULT (getdate()) FOR [Created_Date]
GO

ALTER TABLE [dbo].[PortMaster] ADD  CONSTRAINT [DF_PortMaster_Modified_Date]  DEFAULT (getdate()) FOR [Modified_Date]
GO

ALTER TABLE [dbo].[PortMaster] ADD  CONSTRAINT [DF_PortMaster_Modified_By]  DEFAULT (suser_name()) FOR [Modified_By]
GO

ALTER TABLE [dbo].[PortMaster] ADD  CONSTRAINT [DF_PortMaster_Active]  DEFAULT ((1)) FOR [Active]
GO

ALTER TABLE [dbo].[PortMaster]  WITH NOCHECK ADD  CONSTRAINT [FK_PortMaster_Company] FOREIGN KEY([CompanyRefId])
REFERENCES [dbo].[Company] ([Id])
ON UPDATE CASCADE
ON DELETE CASCADE
NOT FOR REPLICATION 
GO

ALTER TABLE [dbo].[PortMaster] CHECK CONSTRAINT [FK_PortMaster_Company]
GO

