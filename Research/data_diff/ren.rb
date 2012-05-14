9.upto(25) do |n|
  `mv #{sprintf('RainbowWallpaperSettings.java_%03d', n)} #{sprintf('RainbowWallpaperSettings.java_%03d', n-1)}`
end
