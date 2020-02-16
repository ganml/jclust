setwd("~/software/jclust/trunk/jclust/fscgene")
cm <- read.csv("CM0_Fsc.csv")
gene <- read.csv("gene.csv", header=F)
cnames <- unique(cm[,10])
for( c in cnames) {
  ind <- which(cm[,10] == c)
  pdf(paste("gene", c, ".pdf", sep=""), width=4, height=3)
  par(mar=c(3,3,1,1))
  matplot(t(gene[ind, -1]), type = "l", col="black", xlab="", ylab="", ylim=c(-5000, 15000))
  dev.off()
}

