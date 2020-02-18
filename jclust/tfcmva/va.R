setwd("~/Software/jclust/trunk/jclust/tfcmva")
inforce <- read.csv(unzip("inforce.zip", "inforce.csv"))
inforce$age <- (inforce$currentDate- inforce$birthDate)/365
inforce$ttm <- (inforce$matDate- inforce$currentDate)/365

dnames<-c("gmwbBalance","gbAmt","withdrawal",paste("FundValue", 1:10, sep=""),"age","ttm")
xnames<-c("gender","productType")

inforceN <- inforce[,dnames]
vMin <- apply(inforceN, 2, min)
vMax <- apply(inforceN, 2, max)
inforceNS <- (inforceN - matrix(vMin, nrow=nrow(inforceN), ncol= ncol(inforceN), byrow=TRUE)) / matrix(vMax-vMin, nrow=nrow(inforceN), ncol= ncol(inforceN), byrow=TRUE)
inforceC <- model.matrix(~., data= inforce[,xnames])[,-1]

datva <- cbind(recordID=inforce$recordID, inforceNS, inforceC)

# save dataset
saveDS <- function(df, fname) {
  # write data file
  fileConn<-file(paste(fname,".csv",sep=""),"w")
  writeLines(apply(df,1,paste,collapse=","), fileConn)
  close(fileConn)
  
  # write schema file   
  schema <- cbind(names(df),"continuous")
  nVar <- nrow(schema)
  schema[1,2] <- "recordid"
  #schema[nVar,2] <- "class"
  fileConn<-file(paste(fname,".names",sep=""),"w")
  writeLines(c(fname, "///: schema"), fileConn)
  writeLines(apply(schema,1,paste,collapse=","), fileConn)
  close(fileConn)
}
saveDS(datva[,-24], "va190k")
