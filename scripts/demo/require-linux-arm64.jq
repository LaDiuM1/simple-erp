if type != "object" then empty
elif has("os") or has("architecture") then
  select(.os == "linux" and .architecture == "arm64")
else
  .["linux/arm64"]? |
  select(type == "object" and .os == "linux" and .architecture == "arm64")
end
