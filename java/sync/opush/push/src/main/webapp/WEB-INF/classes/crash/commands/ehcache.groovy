import org.crsh.text.ui.UIBuilder
import java.util.concurrent.TimeUnit
import net.sf.ehcache.store.StoreOperationOutcomes.GetOutcome

@Usage("Ehcache commands")
class ehcache extends CRaSHCommand {

  @Usage("Internal usage")
  @Command
  public Date date() {
     return new Date()
  }

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
                                   execute("ehcache date")
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
  public Object conf() {
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")
    def config = getSingleton("org.obm.push.store.ehcache.EhCacheConfiguration")
    def maxMemoryInMB = config.maxMemoryInMB()

    return new UIBuilder().table(separator: dashed, rightCellPadding: 2) {
      row() {
         label("Memory allowed to ehcache ", bold: true, foreground: blue)
         label(maxMemoryInMB + " MB")
      }

      for(String storeName : manager.listStores()) {
          def storePercentage = config.percentageAllowedToCache(storeName)
          row() {
             label(storeName + " ", foreground: blue)
             if (storePercentage.isDefined()) {
                label(storePercentage.applyTo(maxMemoryInMB) + "MB (" + storePercentage.get() + ")")
             } else {
                label("Undefined")
             }
          }
      }
    }
  }

  @Usage("Show statistic about ehcache usage")
  @Command
  public Object stats() {
    def manager = getSingleton("org.obm.push.store.ehcache.ObjectStoreManager")
    def config = getSingleton("org.obm.push.store.ehcache.EhCacheConfiguration")
    def stats = getSingleton("org.obm.push.store.ehcache.EhCacheStatistics")

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
          def storePercentage = config.percentageAllowedToCache(storeName)
          if (!storePercentage.isDefined()) {
              continue
          }
          row() {
             label(storeName)
             storeStats(ui, storeName, stats)
             label(fixRight(byteString(storeMemorySizeInBytes), 6))
             label(percentageBar(storePercentage.applyTo(config.maxMemoryInMB()), storeMemorySizeInBytes))
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
          
