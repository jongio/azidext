variable "basename" {
  type        = string
  description = "The base name for all resources"
  default     = "azidexttest1"
}

variable "location" {
  type        = string
  description = "Azure region where to create resources."
  default     = "West US"
}
