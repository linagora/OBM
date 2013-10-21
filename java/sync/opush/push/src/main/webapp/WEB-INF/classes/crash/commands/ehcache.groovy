import org.crsh.text.ui.UIBuilder
import java.util.concurrent.TimeUnit
import net.sf.ehcache.store.StoreOperationOutcomes.GetOutcome
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage
import org.obm.push.utils.jvm.JvmUtils

@Usage("Ehcache commands")
class ehcache extends CRaSHCommand {

  @Usage("Show ehcache dashboard")
  @Command
  public void dashboard() {
    def config = getSingleton("org.obm.push.store.ehcache.EhCacheConfiguration")
    def table = new UIBuilder().table(columns: [1]) {
        row {
            table(columns: [1]) {
                header() {
                        table() {
                            header(bold: true, fg: red) {
                                label("OPUSH EHCACHE DASHBOARD")
                                label(" ")
                            }
                            row() {
                                label("Refreshing each " + config.statsShortSamplingTimeInSeconds() + " second(s), last refresh at ")
                                eval {
                                   execute("date")
                                }
                            }
                        }
                }
                header() {
                        table() {
                            header(bold: true, fg: red) {
                                label("CONFIGURATION")
                            }
                            row() {
                                eval {
                                   execute("ehcache conf")
                                }
                            }
                        }
                }
                header() {
                        table() {
                            header(bold: true, fg: red) {
                                label("STATISTICS")
                            }
                            row() {
                                eval {
                                   execute("ehcache stats")
                                }
                            }
                        }
                }
            }
       }
    }

    context.takeAlternateBuffer();
    try {
       while (!Thread.interrupted()) {
          out.cls()
          out.show(table)
          out.flush()
          Thread.sleep(TimeUnit.SECONDS.toMillis(config.statsShortSamplingTimeInSeconds()))
       }
    }
    finally {
       context.releaseAlternateBuffer();
    }
  }

  @Usage("Show ehcache configuration")
  @Command
  public Object conf(
            @Usage("Update the maximum ehcache memory")   @Option(names=["update-max"]) Boolean isUpdateMax,
            @Usage("Update percentage allowed to stores") @Option(names=["update-percentages"]) Boolean isUpdatePercentages,
            @Usage("Dump running configuration") @Option(names=["dump"]) Boolean dump) {

    if (isUpdateMax && isUpdatePercentages) {
       return "Sorry, only one option can be given"
    } else if (isUpdateMax) {
       confUpdateMaxMemory()
    } else if (isUpdatePercentages) {
       confUpdateStorePercentages()
    } else if (dump) {
       dumpRunningConfiguration()
    } else {
       confValues()
    }
  }

  private Object confValues() {
    def ui = new UIBuilder()
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")
    def fileConfig = getSingleton("org.obm.push.store.ehcache.EhCacheConfiguration")
    def fileMaxMemoryInMB = fileConfig.maxMemoryInMB()

    def runningConfig= manager.createConfigReader()
    def runningMaxMemoryInMB = runningConfig.getRunningMaxMemoryInMB()
    def runningStoresPercentages = runningConfig.getRunningStoresPercentages()
    def runningStoresMaxMemoryInMB = runningConfig.getRunningStoresMaxMemoryInMB()

    def undefinedLabelOr = {storePercentage, action -> storePercentage.isDefined() ? action(storePercentage) : ui.label("Undefined") }
    def buildFileAbsoluteLabel = { storePercentage -> ui.label(storePercentage.applyTo(fileMaxMemoryInMB)) }
    def buildPercentageLabel = { storePercentage -> ui.label(storePercentage.get()) }

    return ui.table(separator: dashed, rightCellPadding: 2) {
      header(bold: true) {
         label("")
         label("FILE in MB")
         label("FILE in %")
         label("RUNNING in MB")
         label("RUNNING in %")
      }
      row(bold: true, foreground: blue) {
         label("Memory allowed to ehcache ")
         label(fileMaxMemoryInMB)
         label("")
         label(runningMaxMemoryInMB)
         label("")
      }

      for(String storeName : manager.listStores()) {
          def fileStorePercentage = fileConfig.percentageAllowedToCache(storeName)
          row() {
             label(storeName, foreground: blue)
             undefinedLabelOr(fileStorePercentage, buildFileAbsoluteLabel)
             undefinedLabelOr(fileStorePercentage, buildPercentageLabel)
             label(runningStoresMaxMemoryInMB[storeName])
             undefinedLabelOr(runningStoresPercentages[storeName], buildPercentageLabel)
          }
      }
    }
  }

  public void confUpdateMaxMemory() {
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")
    def jvmXmx = JvmUtils.maxRuntimeJvmMemoryInMB()

    manager.createConfigUpdater().updateMaxMemoryInMB(
                readInteger("Give the new ehcache max memory in MiB (JVM has $jvmXmx MiB)"))

    out.println("DONE")
  }

  public void confUpdateStorePercentages() {
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")

    def newPercentagesMap = manager.listStores().collectEntries( [:] ) {
       storeName -> [storeName, Percentage.of(readInteger(storeName))]
    }

    try {
       manager.createConfigUpdater().updateStoresMaxMemory(newPercentagesMap)
       out.println("DONE")
    } catch(e) {
       out.println("ERROR: " + e.getMessage())
       out.println("New values were: " +newPercentagesMap)
    }
  }

  private int readInteger(String message) {
    try {
       readLine(message + ": ").toInteger()
    } catch(e) {
       throw new Exception("An integer was expected", e)
    }
  }

  public void dumpRunningConfiguration() {
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")
    def runningConfig= manager.createConfigReader()
    out.println("### EHCACHE MEMORY SETTINGS")
    out.println("maxMemoryInMB=" + runningConfig.getRunningMaxMemoryInMB())

    out.println()
    out.println("### BY CACHE, IN PERCENT (optional parameters)")    
    def runningStoresPercentages = runningConfig.getRunningStoresPercentages()
    for(String storeName : manager.listStores()) {
      out.println(storeName + "=" + runningStoresPercentages[storeName].getIntValue())
    }
  }
  
  @Usage("Show statistic about ehcache usage")
  @Command
  public Object stats() {
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")
    def config = getSingleton("org.obm.push.store.ehcache.EhCacheConfiguration")
    def stats = getSingleton("org.obm.push.store.ehcache.EhCacheStatistics")
    
    def runningConfig= manager.createConfigReader()
    def runningStoresPercentages = runningConfig.getRunningStoresPercentages()
    def runningStoresMaxMemoryInMB = runningConfig.getRunningStoresMaxMemoryInMB()


    UIBuilder ui = new UIBuilder()
    ui.table(separator: dashed, rightCellPadding: 2) {
       header(bold: true) {
          label("STORE")
          label(diskAccessColumnLabel(config))
          label("MEMORY")
          label("MEMORY BAR")
       }
       for(String storeName : manager.listStores()) {
          def storeMemorySizeInBytes = stats.memorySizeInBytes(storeName)
          def storePercentage = runningStoresPercentages[storeName]
          if (!storePercentage.isDefined()) {
              continue
          }
          row() {
             label(storeName)
             storeStats(ui, storeName, stats)
             label(fixRight(byteString(storeMemorySizeInBytes), 6))
             if (runningStoresMaxMemoryInMB[storeName] != 0) {
               label(percentageBar(runningStoresMaxMemoryInMB[storeName], storeMemorySizeInBytes))
             } else {
               label(percentageBar(storePercentage.applyTo(config.maxMemoryInMB()), storeMemorySizeInBytes))
             }
          }
       }
    }
    return ui;
  }

  private Object getSingleton(String fullyQualifiedName) {
     def singleton = context.attributes.beans[fullyQualifiedName];
     if (singleton == null) {
        return fullyQualifiedName + " not found context";
     }
     return singleton;
  }

  private String fixRight(String label, int charCount) {
     return label.padLeft(charCount, " ")
  }

  private String diskAccessColumnLabel(def config) {
     "DISK HITS /s\n" + 
     fixRight(config.statsShortSamplingTimeInSeconds() + "s|", 3) +  
     fixRight(config.statsMediumSamplingTimeInSeconds() + "s|", 5) +
     fixRight(config.statsLongSamplingTimeInSeconds() + "s", 5)
  }

  private Object storeStats(UIBuilder ui, String storeName, def stats) {
     return ui.label(
        fixRight(notAvailableStringIfException({stats.shortTimeDiskGets(storeName) + "|"}), 3) +
        fixRight(notAvailableStringIfException({stats.mediumTimeDiskGets(storeName) + "|"}), 5) +
        fixRight(notAvailableStringIfException({stats.longTimeDiskGets(storeName) as String}), 5)
     )
  }

  def notAvailableStringIfException(def method) {
     try {
        method()
     } catch(e) {
        return "N/A"
     }
  }

  private String percentageBar(long maxMemoryInMB, long localHeapSizeInBytes) {
     def maxMemoryInBytes = maxMemoryInMB * 1024 * 1024;
     def percentage = ((localHeapSizeInBytes / maxMemoryInBytes) * 100) as int;
     return "".padLeft(percentage/2, "X").padRight(50, ".") + " (" + percentage + "%)";
  }

  private String byteString(long inBytes) {
     if (inBytes < 1024) {
        return inBytes + " BY";
     } else if (inBytes < (1024 * 1024)) {
        return ((inBytes / 1024) as int) + " KB";
     } else {
        return ((inBytes / (1024 * 1024)) as int) + " MB";
     }
  }
}
          
