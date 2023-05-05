##################################################################################################
# WARN  copied&pasted from util.R to make it self-contained
##################################################################################################


# Recursively search for all files given the [filePattern] into the given [directory].
# Return a new single table in which all such data is combined together.
# You might specify some columns with [ignoreColumns] if you need to skip them.
gatherAllTables <- function(directory, ignoreColumns = NULL, filePattern = "^statistics(\\w|-|_|\\.)*\\.csv$") {
  allTables = NULL

  for (table in list.files(directory, recursive = TRUE, full.names = TRUE, pattern = filePattern)) {

    cat("Reading: ", table, "\n")

    tryCatch({ dt <- read.csv(table, header = T) },
             error = function(e) {
               cat("Error in reading table ", table, "\n", paste(e), "\n")
             })

    if (!is.null(ignoreColumns)) {
      for (name in ignoreColumns) {
        dt[name] = NULL
      }
    }

    if (is.null(allTables)) {
      allTables = dt
    } else {
      tryCatch({ allTables = rbind(allTables, dt) },
               error = function(e) {
                 cat("Error in concatenating table ", table, "\n", paste(e), "\n")
               })
    }
  }
  return(allTables)
}

# Recursively gather all data from [directory] given the [filePattern] name.
# Output such data in a zipped file with path [zipFile].
gatherAndSaveData <- function(directory, zipFile, ignoreColumns = NULL, filePattern = "^statistics(\\w|-|_|\\.)*\\.csv$") {
  cat("Loading data...", date(), "\n")

  dt = gatherAllTables(directory, ignoreColumns, filePattern)

  cat("Data is loaded. Starting compressing. ", date(), "\n")

  write.table(dt, file = gzfile(zipFile))

  cat("Data is compressed and saved. Starting reading back. ", date(), "\n")

  table <- read.table(gzfile(zipFile), header = T)

  cat("Data read back. Done! ", date(), "\n")

  return(table)
}

### return a boolean vector, where each position in respect to x is true if that element appear in y
areInTheSubset <- function(x, y) {

  ### first consider vector with all FALSE
  result = x != x
  for (k in y) {
    result = result | x == k
  }
  return(result)
}


### Compute Vargha-Delaney A12 statistics
measureA <- function(a, b) {

  if (length(a) == 0 & length(b) == 0) {
    return(0.5)
  } else if (length(a) == 0) {
    ## motivation is that we have no data for "a" but we do for "b".
    ## maybe the process generating "a" always fail (eg out of memory)
    return(0)
  } else if (length(b) == 0) {
    return(1)
  }

  r = rank(c(a, b))
  r1 = sum(r[seq_along(a)])

  m = length(a)
  n = length(b)
  A = (r1 / m - (m + 1) / 2) / n

  return(A)
}

formateConfigName <- function(config){
  if(identical(config, "internet_t"))
    return("RQ1 (Internet = On)")
  if(identical(config, "internet_f"))
    return("RQ2 (Internet = Off)")
  return(config)
}

removeIfnotnumeric <- function(other){
  other <- other[!is.na(as.numeric(other))]
  return(other)
}

##################################################################################################

EXTRA_CONFIGS = c("internet_t", "internet_f")

EMPLOYED_CONFIG = EXTRA_CONFIGS[2]

# Folder where files like tables and graphs should be saved to
GENERATED_FILES = "../generated_files"

# Where all the CSV files of the experiments are stored
DATA_DIR = "../data"

ROOT_ZIP_DIR = "../"
ZIP_FILE_NAME = "compressedData.zip"
SNAP_ZIP_NAME= "snapshotCompressedData.zip"

# Zip file of the aggregated data
ZIP_FILE = paste(ROOT_ZIP_DIR, EMPLOYED_CONFIG, ZIP_FILE_NAME, sep = "")
SNAP_ZIP_FILE = paste(ROOT_ZIP_DIR, EMPLOYED_CONFIG, SNAP_ZIP_NAME, sep = "")

# collect all data and save to zip
init <- function() {
  ignoreColumns=NULL
  for (conf in EXTRA_CONFIGS) {

    TMP_ZIP_FILE = paste(ROOT_ZIP_DIR, conf, ZIP_FILE_NAME, sep = "")
    TMP_SNAP_ZIP_FILE = paste(ROOT_ZIP_DIR, conf, SNAP_ZIP_NAME, sep = "")

    CONFIG_DATA_DIR = paste(DATA_DIR,"/", conf, sep = "")
    k = gatherAndSaveData(CONFIG_DATA_DIR, TMP_ZIP_FILE, ignoreColumns= ignoreColumns)
    z = gatherAndSaveData(CONFIG_DATA_DIR, TMP_SNAP_ZIP_FILE, ignoreColumns= ignoreColumns, filePattern = "^snapshot(\\w|-|_)*\\.csv$")
  }

}


# print some stats on the data
checkData <- function() {

  ### check for statistics
  for (conf in EXTRA_CONFIGS) {
    cat("\n\n========================================\n")
    cat("Extra Configuration: ", conf,"\n")

    TMP_ZIP_FILE = paste(ROOT_ZIP_DIR, conf, ZIP_FILE_NAME, sep = "")

    dt <- read.table(gzfile(TMP_ZIP_FILE), header = T)
    projects = sort(unique(dt$id))
    labels = sort(unique(dt$labelForExperiments))

    cat("Total projects: ", length(projects),"\n")
    cat("Total configurations: ", length(labels),"\n")
    cat("Total runs: ", length(dt$algorithm), "\n")


    for (proj in projects) {
      cat("\n\n-------------------------------------\n")
      cat("Project", proj, "\n")
      projMask = dt$id == proj

      for(conf in labels){
        confMask = dt$labelForExperiments == conf

        cat("CONFIGURATION:",conf,"\n")
        checkProject(dt,projMask & confMask)
      }
    }

    cat("\n\n-------------------------------------\n")
  }
}

checkProject <- function(dt,mask){

  data = dt$coveredLines[mask]
  seconds = dt$elapsedSeconds[mask]

  cat("Time (seconds):", mean(seconds), "\n")
  cat("Evaluated actions:", mean(dt$evaluatedActions[mask]), "\n")
  cat("Runs: ", length(seconds), "\n")
  cat("Coverage (lines): ", mean(data), "(", min(data), "-", max(data), ") \n")
  cat("\traw values: [",sort(data),"]\n")
  cat("\n\n")
}


checkProjectSnapshot <- function(dt,mask){

  targets = sort(unique(dt$interval))
  z = length(targets)

  for (i in 1:z) {
    targetMask = dt$interval == targets[[i]]
    data = dt$seed[targetMask & mask]
    cat("interval ",i, ":", length(data), sep = "")
    cat("\traw values: [",data,"]\n")
    cat("\n\n")
  }

}

allInOneTableComparision <- function(){
  dt <- NULL
  snapdt <- NULL
  for (conf in EXTRA_CONFIGS) {

    TMP_ZIP_FILE = paste(ROOT_ZIP_DIR, conf, ZIP_FILE_NAME, sep = "")

    ds <- read.table(gzfile(TMP_ZIP_FILE), header = T)
    ds['conf'] = rep(conf, nrow(ds))
    if(is.null(dt)){
      dt <- ds
    }else{
      dt <- rbind(dt, ds)
    }

    TMP_SNAP_ZIP_FILE = paste(ROOT_ZIP_DIR, conf, SNAP_ZIP_NAME, sep = "")
    snapds <- read.table(gzfile(TMP_SNAP_ZIP_FILE), header = T)
    snapds['conf'] = rep(conf, nrow(snapds))
    if(is.null(snapdt)){
      snapdt <- snapds
    }else{
      snapdt <- rbind(snapdt, snapds)
    }
  }

  baseMask = dt$labelForExperiments == "_NONE_false"
  wmClosetMask = dt$labelForExperiments == "_USER_true_50_10_CLOSEST_SAME_PATH_90"
  wmRandomMask = dt$labelForExperiments == "_USER_true_50_10_RANDOM_90"

  projects = sort(unique(dt$id))

  tableComparisonWithAllConfigs(dt, projects, baseMask, wmRandomMask, "Base", "WM", paste("allconfigs_","_wm_rand", sep = ""))
  coverageBoxplot(dt,"ind1",EXTRA_CONFIGS[1], baseMask, wmRandomMask, "Base", "WM","ind1")

  coveragePlot(snapdt, "_NONE_false", "_USER_true_50_10_RANDOM_90", "coverage_plot")
}

tableComparisonWithAllConfigs <- function(dt, projects, xMask, yMask, xName, yName, tableSuffix) {

  TABLE = paste(GENERATED_FILES, "/tableComparison", tableSuffix, ".tex", sep = "")
  unlink(TABLE)
  sink(TABLE, append = TRUE, split = TRUE)

  cat("\\begin{tabular}{ l rrrr rrrr | rrrr rrrr}\\\\ \n")
  cat("\\toprule \n")
  cat(" & ", "\\multicolumn{8}{c}{",formateConfigName(EXTRA_CONFIGS[1]),"}"," & ", "\\multicolumn{8}{c}{",formateConfigName(EXTRA_CONFIGS[2]),"}", "\\\\  \n" ,sep = "")
  cat(" & \\multicolumn{4}{c}{Line Coverage \\%} & \\multicolumn{4}{c}{Detected Faults \\%} & \\multicolumn{4}{c}{Line Coverage \\%} & \\multicolumn{4}{c}{Detected Faults \\%} \\\\  \n")
  cat("SUT & ",xName," & ",yName,"  & $\\hat{A}_{12}$ & $p$-value  & ",xName," & ",yName, " & $\\hat{A}_{12}$ & $p$-value", "&",xName," & ",yName,"  & $\\hat{A}_{12}$ & $p$-value  & ",xName," & ",yName, " & $\\hat{A}_{12}$ & $p$-value", " \\\\ \n", sep = "")

  cat("\\midrule \n")

  avgLines = matrix(nrow = length(projects), ncol = 3 * length(EXTRA_CONFIGS))
  avgFaults = matrix(nrow = length(projects), ncol = 3 * length(EXTRA_CONFIGS))



  for (i in 1:length(projects)) {
    proj = projects[[i]]

    cat("\\emph{", proj, "} ", sep = "")

    projectMask =  dt$id == proj

    tot = max(dt$numberOfLines[dt$id == proj])


    for (c in 1:length(EXTRA_CONFIGS)) {
      conf = EXTRA_CONFIGS[c]
      configMask = dt$conf == conf

      baseMask = xMask
      otherMask = yMask

      base = dt$coveredLines[configMask & projectMask & baseMask]
      other = dt$coveredLines[configMask & projectMask & otherMask]

      base = 100 * (base / tot)
      other = 100 * (other / tot)
      if(length(base) > 0 & length(other) > 0){
        avgLines[i, (c-1)*3 + 1] = mean(base)
        avgLines[i, (c-1)*3 + 2] = mean(other)
        avgLines[i, (c-1)*3 + 3] = measureA(other, base)
      }
      printComparison(base, other)

      base = dt$potentialFaults[configMask & projectMask & baseMask]
      other = dt$potentialFaults[configMask & projectMask & otherMask]
      if(length(base) > 0 & length(other) > 0){
        avgFaults[i, (c-1)*3 + 1] = mean(base)
        avgFaults[i, (c-1)*3 + 2] = mean(other)
        avgFaults[i, (c-1)*3 + 3] = measureA(other, base)
      }
      printComparison(base, other)


    }

    cat(" \\\\ \n")

  }
  cat("\\midrule \n")
  cat("Average ")
  printAverageComparison(avgLines)
  printAverageComparison(avgFaults)

  printAverageComparison(avgLines, 3)
  printAverageComparison(avgFaults, 3)
  cat(" \\\\ \n")

  cat("\\bottomrule \n")
  cat("\\end{tabular} \n")

  sink()
}

tableComparison <- function(dt, projects, xMask, yMask, xName, yName, tableSuffix) {

  TABLE = paste(GENERATED_FILES, "/tableComparison", tableSuffix, ".tex", sep = "")
  unlink(TABLE)
  sink(TABLE, append = TRUE, split = TRUE)

  cat("\\begin{tabular}{ l rrrr  rrrr}\\\\ \n")
  cat("\\toprule \n")
  cat("SUT & \\multicolumn{4}{c}{Line Coverage \\%} & \\multicolumn{4}{c}{Detected Faults \\%} \\\\  \n")
  cat("    & ",xName," & ",yName,"  & $\\hat{A}_{12}$ & $p$-value  & ",xName," & ",yName, " & $\\hat{A}_{12}$ & $p$-value \\\\ \n", sep = "")

  avgLines = matrix(nrow = length(projects), ncol = 3) # X, Y and A12
  avgFaults = matrix(nrow = length(projects), ncol = 3) # X, Y and A12

  for (i in 1:length(projects)) {
    proj = projects[[i]]
    cat("\\midrule \n")
    cat("\\emph{", proj, "} ", sep = "")

    projectMask =  dt$id == proj
    baseMask = xMask
    otherMask = yMask

    base = dt$coveredLines[projectMask & baseMask]
    other = dt$coveredLines[projectMask & otherMask]
    tot = max(dt$numberOfLines[dt$id == proj])
    base = 100 * (base / tot)
    other = 100 * (other / tot)
    avgLines[i, 1] = mean(base)
    avgLines[i, 2] = mean(other)
    avgLines[i, 3] = measureA(other, base)
    printComparison(base, other)

    base = dt$potentialFaults[projectMask & baseMask]
    other = dt$potentialFaults[projectMask & otherMask]
    avgFaults[i, 1] = mean(base)
    avgFaults[i, 2] = mean(other)
    avgFaults[i, 3] = measureA(other, base)
    printComparison(base, other)

    cat(" \\\\ \n")
  }
  cat("\\midrule \n")
  cat("Average ")
  printAverageComparison(avgLines)
  printAverageComparison(avgFaults)
  cat(" \\\\ \n")

  cat("\\bottomrule \n")
  cat("\\end{tabular} \n")

  sink()
}

printAverageComparison <- function(data, startIndex=0) {
  base = data[,startIndex+1]
  base <- base[!is.na(as.numeric(base))]
  other = data[,startIndex+2]
  other <- other[!is.na(as.numeric(other))]
  A=data[,startIndex+3]
  A <- A[!is.na(as.numeric(A))]
  cat(" & ")
  cat(formatC(mean(base), digits = 1, format = "f"), sep = "")
  cat(" & ")
  cat(formatC(mean(other), digits = 1, format = "f"), sep = "")
  cat(" & ")
  cat(formatC(mean(A), digits = 2, format = "f"), sep = "")
  cat(" & ") # no pvalue
}

printComparison <- function(base, other) {
  cat(" & ")

  if(length(base) == 0)
    cat("-")
  else
    cat(formatC(mean(base), digits = 1, format = "f"), sep = "")

  cat(" & ")
  if(length(other) == 0)
    cat("-")
  else
    cat(formatC(mean(other), digits = 1, format = "f"), sep = "")

  if(length(base) == 0 || length(other) == 0){
    cat(" & - & - ")
  } else{
    A = measureA(other, base)

    if (length(other) == 0 | length(base) == 0)
      p = 1
    else {
      w = wilcox.test(other, base)
      p = w$p.value
    }
    significant = FALSE

    if (is.nan(p)) {
      p = "1.000"
    } else if (p < 0.001) {
      significant = TRUE
      p = "$< 0.001$"
    } else {
      if(p <= 0.05){
        significant = TRUE
      }
      p = formatC(p, digits = 3, format = "f")
    }

    cat(" & ")
    if(significant)
      cat("{\\bf ",sep="")
    cat(formatC(A, digits = 2, format = "f"), sep = "")
    if(significant)
      cat("}",sep="")

    cat(" & ")
    cat(p)
  }
}


coverageBoxplot <- function(dt, project, config, xMask, yMask, xName, yName, plotSuffix){
  projectMask =  dt$id == project
  configMask = dt$conf == config
  baseMask = xMask
  otherMask = yMask

  tot = max(dt$numberOfLines[dt$id == project])

  base = dt$coveredLines[configMask & projectMask & baseMask]
  other = dt$coveredLines[configMask & projectMask & otherMask]

  base = 100 * (base / tot)
  other = 100 * (other / tot)

  df<-data.frame(Y=c(base,other),Grp=rep(c(xName,yName),times=c(length(base),length(other))))

  pdf(paste(GENERATED_FILES, "/", plotSuffix,"_",project,"_boxplot.pdf", sep = ""))

  boxplot(Y ~ Grp, data=df, xlab = "Techniques",
          ylab = "Line coverage %")
  dev.off()
}


coveragePlot <- function(dt, xlabel, ylabel, prefix){
  projects = sort(unique(dt$id))
  for (project in projects) {
    projectMask =  dt$id == project
    baseMask = dt$labelForExperiments == xlabel
    otherMask = dt$labelForExperiments == ylabel

    onMask = dt$conf == EXTRA_CONFIGS[1]
    offMask = dt$conf == EXTRA_CONFIGS[2]

    tot = max(dt$numberOfLines[dt$id == project])

    targets = sort(unique(dt$interval))
    z = length(targets)



    onBase = rep(0, times = z)
    onOther = rep(0, times = z)
    offBase = rep(0, times = z)
    offOther = rep(0, times = z)

    for (i in 1 : z) {
      targetMask = dt$interval == targets[[i]]
      allOnBase = dt$coveredLines[targetMask & onMask & projectMask & baseMask]
      allOnOther = dt$coveredLines[targetMask & onMask & projectMask & otherMask]
      allOffBase = dt$coveredLines[targetMask & offMask & projectMask & baseMask]
      allOffOther = dt$coveredLines[targetMask & offMask & projectMask & otherMask]

      if(length(onBase) > 0){
        onBase[[i]] = mean(allOnBase)
        onOther[[i]] = mean(allOnOther)
      }
      offBase[[i]] = mean(allOffBase)
      offOther[[i]] = mean(allOffOther)
    }




    off_colors = c("cyan", "black")
    on_colors = c("red", "blue")
    plot_colors = c(off_colors, on_colors)

    line_width = 2
    pdf(paste(GENERATED_FILES, "/", prefix,"_",project,".pdf", sep = ""))

    if(identical(project, "catwatch")){
      yMin = min(offBase,offOther)
      yMax = max(offBase,offOther)

      plot(offBase, ylim = c(yMin, yMax), type = "o", col = off_colors[[1]], pch = 21, lty = 1, lwd = line_width, ylab = "#Covered Lines", xlab = "Budget Percentage", xaxt = "n")
      axis(side = 1, labels = targets, at = 1 : z)


      lines(offOther, type = "o", col = off_colors[[2]], pch = 22, lty = 2, lwd = line_width)

      lx = 15
      ly = yMin + 0.8 * (yMax - yMin)

      legend(lx, ly, c("Off-Base","Off-WM")
             , cex = 1.2, col = off_colors
             , pch = 21 : 22
             , lty = 1 : 2)

      dev.off()
    }else{
      yMin = min(onBase,onOther,offBase,offOther)
      yMax = max(onBase,onOther,offBase,offOther)

      plot(onBase, ylim = c(yMin, yMax), type = "o", col = plot_colors[[1]], pch = 21, lty = 1, lwd = line_width, ylab = "#Covered Lines", xlab = "Budget Percentage", xaxt = "n")
      axis(side = 1, labels = targets, at = 1 : z)

      lines(onOther, type = "o", col = plot_colors[[2]], pch = 22, lty = 2, lwd = line_width)

      lines(offBase, type = "o", col = plot_colors[[3]], pch = 23, lty = 3, lwd = line_width)

      lines(offOther, type = "o", col = plot_colors[[4]], pch = 24, lty = 4, lwd = line_width)

      lx = 15

      ly = yMin + 0.8 * (yMax - yMin)
      if(identical(project,"ind1")){
        ly = yMin + 0.3 * (yMax - yMin)
      }

      legend(lx, ly, c("On-Base", "On-WM", "Off-Base","Off-WM")
             , cex = 1.2, col = plot_colors
             , pch = 21 : 24
             , lty = 1 : 4)

      dev.off()
    }
  }

}

allInOneTableComparision()
