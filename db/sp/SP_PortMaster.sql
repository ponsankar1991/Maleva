USE [LiveMaleva2]
GO

/****** Object:  StoredProcedure [dbo].[SP_PortMaster]    Script Date: 15-02-2026 12.33.24 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE PROCEDURE [dbo].[SP_PortMaster]
	@details nvarchar(max),
	@Comid int
	
AS
BEGIN
    declare	@Result int=0;
	declare @msg varchar(100)='';
	declare @flag int=1;
	declare	@idnew int=0;
	declare @Brandnew varchar(100)='';
 BEGIN TRY	
	 BEGIN TRANSACTION	

	   IF OBJECT_ID('tempdb..#temp') IS NOT NULL    drop TABLE tempdb..#temp
		 SELECT * 
         INTO #temp
         FROM 
(SELECT ROW_NUMBER() OVER(ORDER BY Id) AS tempid,Id,PortName,Active
        FROM OPENJSON(@details)
        WITH (	
		Id int '$.Id',			
		PortName varchar(100) '$.PortName',
		Active int '$.Active'
        )) AS x

declare	@Id int
declare	@tempid int
declare	@PortName varchar(100)
declare	@Active int


		WHILE EXISTS (SELECT * FROM #Temp)
  BEGIN
    SELECT TOP 1 
@Id=Id,
@PortName=PortName,
@Active=Active,
@tempid=tempid
from #Temp


if(@Id=0)
   BEGIN
     insert into PortMaster(CompanyRefId,PortName,Created_Date,Modified_Date,Modified_By,Active)
	 values(@Comid,@PortName,getdate(),getdate(),(suser_name()),@Active)
	 set @Id = (select Scope_identity() AS IdNew)		
     
   END
ELSE
   BEGIN
    update PortMaster set
	PortName= @PortName,
	Active=@Active
	where Id=@Id
   END

   	  delete from #Temp where tempid=@tempid
END
	 COMMIT TRAN -- Transaction Success!
	 SET @Result =1 
	select @Result as Result,@msg as Msg,@Id as Id,@Brandnew As AccountName;
	END TRY
	BEGIN CATCH		
		IF @@TRANCOUNT > 0
					DECLARE @Message varchar(MAX) = ERROR_MESSAGE();
			ROLLBACK TRAN --RollBack in case of Error
		SET @Result =0 
		select @Result as Result,@Message as Msg,@Id as Id,@Brandnew As AccountName;
	END CATCH
END
GO

