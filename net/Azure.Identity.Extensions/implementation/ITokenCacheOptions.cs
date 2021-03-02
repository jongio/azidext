// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

using System;
using System.Collections.Generic;
using System.Text;

namespace Azure.Identity.Extensions.implementation
{
    internal interface ITokenCacheOptions
    {
        TokenCache TokenCache { get; }
    }
}
