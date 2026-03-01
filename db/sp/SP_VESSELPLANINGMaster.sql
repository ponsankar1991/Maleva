USE [LiveMaleva2]
GO

/****** Object:  StoredProcedure [dbo].[SP_VESSELPLANINGMaster]    Script Date: 1/3/2026 2:10:41 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO




CREATE PROCEDURE [dbo].[SP_VESSELPLANINGMaster]
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
		 (SELECT ROW_NUMBER() OVER(ORDER BY SNo) AS tempid,Id,CompanyRefId,EmployeeRefId,UserRefId,SaleDate,FDate,TDate,Remarks,Search,CNumberDisplay,CNumber,SaleDetails
		  FROM OPENJSON(@master)
        WITH (
		SNo int '$.boundindex', 
		Id int '$.Id',
		CompanyRefId int '$.CompanyRefId',
		EmployeeRefId int '$.EmployeeRefId',	
		UserRefId int '$.UserRefId',	
		FDate date '$.FDate',	
		TDate date '$.TDate',	
		SaleDate date '$.SaleDate',
		Remarks varchar(2000) '$.Remarks',
		Search varchar(300) '$.Search',
		
		CNumberDisplay varchar(10) '$.CNumberDisplay',
	    CNumber int '$.CNumber',
		SaleDetails nvarchar(max) '$.SaleDetails' AS JSON
		)) AS x

		declare @Id int 
		declare @CompanyRefId int 
		
	declare @EmployeeRefId int 	
	declare @UserRefId int 	
	declare @FDate date 
	declare @TDate date 
	declare @SaleDate date 

	declare @Remarks varchar(2000) 
	declare @Search varchar(300) 
	
		declare @CNumberDisplay varchar(50)
	declare @CNumber int
	declare @tempid int
	declare @SaleNo int
	declare @SaleNoDisplay varchar(50)
		declare @SaleDetails nvarchar(max)

 -- Start Process
	   
	   
		   WHILE EXISTS (SELECT * FROM #temp1)
           BEGIN
		      SELECT TOP 1 
		 @tempid =tempid,
 @Id =Id ,
 @CompanyRefId=CompanyRefId,
@EmployeeRefId =EmployeeRefId ,
@UserRefId =UserRefId ,
@FDate=FDate,
@TDate=TDate,
@SaleDate =SaleDate ,
@Search=Search,
@Remarks=Remarks,
@CNumberDisplay = CNumberDisplay,
@CNumber = CNumber,
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
	  
	
	 
	  

	  

	   -- Edit Process
	   if @Id <>0 
	     BEGIN
			 delete from VESSELPLANINGDetails where VESSELPLANINGMasterRefId=@Id 
		 END

   --VESSELPLANINGMaster Insert
     if @Id =0
	    BEGIN
		    insert into VESSELPLANINGMaster([CompanyRefId] ,[UserRefId] ,[EmployeeRefId] ,[LastEmployeeRefId] ,[FDate],[TDate] ,[SaleDate],[Search] ,[Remarks],[Active] ,[Created_Date] ,[Created_By] ,[Modified_Date] ,[Modified_By] ,[CNumberDisplay],[CNumber])
			                     values(@CompanyRefId,@UserRefId,@EmployeeRefId,@EmployeeRefId,@FDate,@TDate,@SaleDate,@Search,@Remarks,1,getdate(),(suser_name()),getdate(),(suser_name()),@CNumberDisplay,@CNumber)
		set @saleid = (select Scope_identity() AS IdNew)
		END  
    else
	   BEGIN
	     set  @saleid = @Id


	       Update VESSELPLANINGMaster
		   set 
LastEmployeeRefId =@EmployeeRefId ,
UserRefId =@UserRefId ,
FDate=@FDate,
TDate=@TDate,
SaleDate =@SaleDate ,
Search=@Search,
Remarks=@Remarks
--CNumberDisplay=@CNumberDisplay,
--CNumber=@CNumber

where id=@id
	   END

      -- SaleDetails Insert

		 INSERT INTO VESSELPLANINGDetails ([VESSELPLANINGMasterRefId],[SaleOrderMasterRefId],[Remarks],[Created_Date],[Modified_Date])
         SELECT @saleid as VESSELPLANINGMasterRefId,SaleOrderMasterRefId ,Remarks,GETDATE(),GETDATE()
    FROM OPENJSON(@SaleDetails)
    WITH (	   
	SaleOrderMasterRefId int '$.SaleOrderMasterRefId',    
	Remarks varchar(200) '$.Remarks'
	 )
 IF @id =0  
     BEGIN    
        
      
				   SET @count = (SELECT ISNULL(MAX(SequenceNo),0) FROM  SequenceNoMaster  Where CompanyRefId = @CompanyRefId and SequenceName='VESSELPLANINGMaster')     
				       IF @count =0       
					         BEGIN          
					       
										     update VESSELPLANINGMaster set CNumber = 1 where Id = @saleid       
											       SET @SaleNo =1     
												   update SequenceNoMaster set SequenceNo=@SaleNo Where CompanyRefId = @CompanyRefId and SequenceName='VESSELPLANINGMaster'
									  END
										  Else       
										     BEGIN           
											     SET @SaleNo = (SELECT ISNULL(MAX(SequenceNo)+1,1) FROM  SequenceNoMaster  Where CompanyRefId = @CompanyRefId and SequenceName='VESSELPLANINGMaster')
												 update VESSELPLANINGMaster set CNumber = @SaleNo where Id = @saleid       								
			                                     update SequenceNoMaster set SequenceNo=@SaleNo Where CompanyRefId = @CompanyRefId and SequenceName='VESSELPLANINGMaster'
											 END              


					SET @SaleNoDisplay =  'VPL'+RIGHT('000000000'+cast(@SaleNo as varchar(50))  ,9)        
			
	     update VESSELPLANINGMaster set CNumberDisplay = @SaleNoDisplay where Id = @saleid  

	



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

