#pragma once

#include <array>
#include <memory>

#include "engine.h"
#include "random/random.h"    // <— include your concrete engines here

// inline gives it *one* definition across all TUs
static std::array<std::unique_ptr<Engine>, 1> engines = {
    std::make_unique<Random>()
    // , std::make_unique<OtherEngine>(…)
};